# coding: utf-8
from data_utils import get_data
from config import Config
import tensorflow as tf
import time
import os
import utils
from char_rnn_net import char_rnn_net

LEARNING_RATE = 1e-3


def train():
    save_path = Config.model_path
    data, word2ix, ix2word = get_data(Config)
    num_classes = len(word2ix)
    input_batch, target_batch = utils.generate_batch_data(input=data, batch_size=Config.batch_size)
    endpoints = char_rnn_net(input_batch, num_classes)

    with tf.name_scope('loss'):
        labels = tf.reshape(target_batch, [Config.batch_size * target_batch.shape[0]])
        loss = tf.nn.sparse_softmax_cross_entropy_with_logits(logits=endpoints['output'], labels=labels)
        loss = tf.reduce_mean(loss)

    with tf.name_scope('accuracy'):
        proba_prediction = tf.nn.softmax(endpoints['output'], name='output_probability')
        prediction = tf.argmax(proba_prediction, axis=1, name='output_prediction')
        prediction = tf.cast(prediction, dtype=tf.int32)
        correct_predictions = tf.equal(prediction, labels)
        accuracy = tf.reduce_mean(tf.cast(correct_predictions, tf.float32))

    optimizer = tf.train.AdamOptimizer(learning_rate=LEARNING_RATE).minimize(loss)

    saver = tf.train.Saver(tf.global_variables())
    init_op = tf.global_variables_initializer()
    with tf.Session() as sess:
        sess.run(init_op)
        global_step = 0
        latest_ck_file = tf.train.latest_checkpoint(save_path)
        if latest_ck_file:
            print('restore from latest checkpoint file : {}'.format(latest_ck_file))
            global_step += int(latest_ck_file.split('-')[-1])
            saver.restore(sess, latest_ck_file)

        max_steps = int(len(data) / Config.batch_size * Config.epoch)
        try:
            for step in range(max_steps):
                global_step = global_step + 1
                start = time.time()
                batch_loss, accuracy_, _, _ = sess.run([loss, accuracy, endpoints['hidden'], optimizer])
                end = time.time()
                if step % 10 == 0:
                    print('step: {}/{} '.format(step, max_steps),
                          'loss: {:.4f} '.format(batch_loss),
                          'accuracy: {:.2f} '.format(accuracy_),
                          '{:.4f} sec/batch '.format((end - start)))
                if step % 1000 == 0 or step >= max_steps:
                    saver.save(sess, os.path.join(save_path, 'model'), global_step=global_step)
        except KeyboardInterrupt:
            saver.save(sess, os.path.join(save_path, 'model'), global_step=global_step)


if __name__ == '__main__':
    train()

