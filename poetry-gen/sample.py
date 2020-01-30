#!/usr/bin/python
# coding=utf8

from char_rnn_net import char_rnn_net
from config import Config
from data_utils import get_data
import tensorflow as tf
from utils import pick_top_n


def gen_acrostic(start_words, word2ix, ix2word, prefix_words=None):
    with tf.Session() as sess:
        save_path = Config.model_path
        num_classes = len(word2ix)
        inputs = tf.placeholder(tf.int32, shape=(1, 1), name="inputs")
        endpoints = char_rnn_net(inputs, num_classes, is_training=False)
        output_tensor = endpoints['output']
        output_tensor = tf.nn.softmax(output_tensor)

        results = []
        start_word_len = len(start_words)
        # 手动设置第一个词为<START>
        pre_word = '<START>'
        start = [[word2ix[pre_word]]]
        index = 0

        saver = tf.train.Saver(tf.global_variables())
        init_op = tf.group(tf.global_variables_initializer(), tf.local_variables_initializer())

        sess.run(init_op)

        latest_ck_file = tf.train.latest_checkpoint(save_path)
        if latest_ck_file:
            print('restore from latest checkpoint file : {}'.format(latest_ck_file))
            saver.restore(sess, latest_ck_file)
        else:
            print('no checkpoint file to restore, exit()')
            exit()

        new_state = sess.run(endpoints['initial_state'])

        if prefix_words:
            for word in prefix_words:
                feed = {endpoints['initial_state']: new_state, inputs: start}
                output, new_state = sess.run([output_tensor, endpoints['hidden']], feed_dict=feed)
                start = [[word2ix[word]]]

        for i in range(Config.max_gen_len):
            feed = {endpoints['initial_state']: new_state, inputs: start}
            output, new_state = sess.run([output_tensor, endpoints['hidden']], feed_dict=feed)
            top_index = pick_top_n(output[0], num_classes)
            w = ix2word[top_index]
            if pre_word in {u'。', u'！', '<START>'}:
                # 如果遇到句号，藏头的词送进去生成
                if index == start_word_len:
                    # 如果生成的诗歌已经包含全部藏头的词，则结束
                    break
                else:
                    # 把藏头的词作为输入送入模型
                    w = start_words[index]
                    index += 1
                    start = [[word2ix[w]]]
            else:
                # 否则的话，把上一次预测是词作为下一个词输入
                start = [[word2ix[w]]]
            results.append(w)
            pre_word = w
    return results


def generate(start_words, word2ix, ix2word, prefix_words=None):
    """
    给定几个词，根据这几个词接着生成一首完整的诗歌
    start_words：u'春江潮水连海平'
    比如start_words 为 春江潮水连海平，可以生成：
    """
    with tf.Session() as sess:
        save_path = Config.model_path
        num_classes = len(word2ix)
        inputs = tf.placeholder(tf.int32, shape=(1, 1), name="inputs")
        endpoints = char_rnn_net(inputs, num_classes, is_training=False)
        output_tensor = endpoints['output']
        output_tensor = tf.nn.softmax(output_tensor)
        # output_tensor = tf.argmax(output_tensor, 1)

        results = list(start_words)
        start_word_len = len(start_words)
        # 手动设置第一个词为<START>
        start = [[word2ix['<START>']]]

        saver = tf.train.Saver(tf.global_variables())
        init_op = tf.group(tf.global_variables_initializer(), tf.local_variables_initializer())

        sess.run(init_op)

        latest_ck_file = tf.train.latest_checkpoint(save_path)
        if latest_ck_file:
            print('restore from latest checkpoint file : {}'.format(latest_ck_file))
            saver.restore(sess, latest_ck_file)
        else:
            print('no checkpoint file to restore, exit()')
            exit()

        new_state = sess.run(endpoints['initial_state'])

        if prefix_words:
            for word in prefix_words:
                feed = {endpoints['initial_state']: new_state, inputs: start}
                output, new_state = sess.run([output_tensor, endpoints['hidden']], feed_dict=feed)
                start = [[word2ix[word]]]
        for i in range(Config.max_gen_len):
            feed = {endpoints['initial_state']: new_state, inputs: start}
            output, new_state = sess.run([output_tensor, endpoints['hidden']], feed_dict=feed)
            if i < start_word_len:
                w = results[i]
                start = [[word2ix[w]]]
            else:
                index = pick_top_n(output[0], num_classes)
                w = ix2word[index]
                results.append(w)
                start = [[index]]
            if w == '<EOP>':
                del results[-1]
                break
    return results


if __name__ == '__main__':
    data, word2ix, ix2word = get_data(Config)
    result = generate(u'春江潮水连海平', word2ix, ix2word, prefix_words=u'郡邑浮前浦，波澜动远空。')
    print(''.join(result))

