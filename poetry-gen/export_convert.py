#!/usr/bin/python
# coding=utf8

from tensorflow import flags
from data_utils import get_data
from config import Config
import json
import codecs

flags.DEFINE_string('convert_file', './convert.json',
                    'convert file')

FLAGS = flags.FLAGS

if __name__ == '__main__':
    data, word2ix, ix2word = get_data(Config)
    convert = {
        'word2ix': word2ix,
        'ix2word': ix2word
    }
    fp = codecs.open(FLAGS.convert_file, 'a+', 'utf-8')
    jsonStr = json.dumps(convert, indent=4, ensure_ascii=False)
    fp.write(jsonStr)
    fp.close()
    print('save convert file finished')

