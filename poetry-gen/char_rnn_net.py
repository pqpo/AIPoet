# coding: utf-8
import tensorflow as tf


# [num_seqs, batch_size]
def char_rnn_net(inputs, num_classes, batch_size=128, is_training=True, num_layers=2, lstm_size=256, embedding_size=128):

    if not is_training:
        batch_size = 1

    with tf.name_scope('embedding'):
        embedding = tf.Variable(tf.truncated_normal(shape=[num_classes, embedding_size], stddev=0.1), name='embedding')
        lstm_inputs = tf.nn.embedding_lookup(embedding, inputs)

    with tf.name_scope('lstm'):
        cell = tf.nn.rnn_cell.MultiRNNCell(
            [tf.nn.rnn_cell.LSTMCell(lstm_size, state_is_tuple=is_training) for _ in range(num_layers)],
            state_is_tuple=is_training)
        initial_state = cell.zero_state(batch_size, dtype=tf.float32)

        x_sequence = tf.unstack(lstm_inputs)
        lstm_outputs, hidden = tf.nn.static_rnn(cell, x_sequence, initial_state=initial_state)

        x = tf.reshape(lstm_outputs, [-1, lstm_size])
        output = tf.layers.dense(inputs=x, units=num_classes, activation=None)
        endpoints = {'output': output, 'hidden': hidden, 'initial_state': initial_state}
        return endpoints
