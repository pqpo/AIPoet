#!/usr/bin/python
# coding=utf8

from char_rnn_net import char_rnn_net
import tensorflow as tf
from tensorflow import flags
from data_utils import get_data
from config import Config

flags.DEFINE_string('output_file', './poetry_gen_lite_model_quantize.tflite',
                    'Output file')


FLAGS = flags.FLAGS

if __name__ == "__main__":
    checkpoints_path = Config.model_path
    data, word2ix, ix2word = get_data(Config)
    num_classes = len(word2ix)
    inputs = tf.placeholder(tf.int32, shape=(1, 1), name="inputs")
    endpoints = char_rnn_net(inputs, num_classes, is_training=False)
    initial_state = endpoints['initial_state']

    output_tensor = endpoints['output']
    output_tensor = tf.nn.softmax(output_tensor)
    output_tensor = tf.argmax(output_tensor, 1, name='outputs', output_type=tf.int32)

    hidden = endpoints['hidden']

    print('###1.1 input shape is: {}, name is: {}, type is: {}'.format(inputs.get_shape(), inputs.name, inputs.dtype))
    print('###1.2 input shape is: {}, name is: {}, type is: {}'.format(initial_state.get_shape(), initial_state.name, initial_state.dtype))
    print('###2.1 output shape is: {}, name is: {}, type is: {}'.format(output_tensor.get_shape(), output_tensor.name, output_tensor.dtype))
    print('###2.2 output shape is: {}, name is: {}, type is: {}'.format(hidden.get_shape(), hidden.name, hidden.dtype))

    # Saver
    saver = tf.train.Saver(tf.global_variables())
    init_op = tf.group(tf.global_variables_initializer(), tf.local_variables_initializer())

    with tf.Session() as sess:
        sess.run(init_op)

        latest_ck_file = tf.train.latest_checkpoint(checkpoints_path)
        if latest_ck_file:
            print('restore from latest checkpoint file : {}'.format(latest_ck_file))
            saver.restore(sess, latest_ck_file)
        else:
            print('no checkpoint file to restore, exit()')
            exit()

        print('### test model'.format(inputs.get_shape(), inputs.name))

        start_words = u'春眠不觉晓'
        prefix_words = u'床前明月光，疑似地上霜。'
        results = list(start_words)
        start_word_len = len(start_words)
        # 手动设置第一个词为<START>
        start = [[word2ix['<START>']]]
        new_state = sess.run(endpoints['initial_state'])

        if prefix_words:
            for word in prefix_words:
                feed = {initial_state: new_state, inputs: start}
                output, new_state = sess.run([output_tensor, hidden], feed_dict=feed)
                start = [[word2ix[word]]]
        for i in range(Config.max_gen_len):
            feed = {initial_state: new_state, inputs: start}
            output, new_state = sess.run([output_tensor, hidden], feed_dict=feed)
            if i < start_word_len:
                w = results[i]
                start = [[word2ix[w]]]
            else:
                index = output[0]
                w = ix2word[index]
                results.append(w)
                start = [[index]]
            if w == '<EOP>':
                del results[-1]
                break

        print(''.join(results))

        converter = tf.contrib.lite.TFLiteConverter.from_session(sess, [inputs, initial_state],
                                                                 [output_tensor, hidden])
        converter.post_training_quantize = True
        tflite_model = converter.convert()
        open(FLAGS.output_file, 'wb').write(tflite_model)
        print('save tflite model finished')




