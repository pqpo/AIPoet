package me.pqpo.aipoet

import java.lang.Exception

class UnmappedWordException(val word:String): Exception("unmapped word($word) to index") {
}