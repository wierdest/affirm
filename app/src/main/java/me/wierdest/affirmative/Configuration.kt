package me.wierdest.affirmative

class Configuration {
    companion object {
        const val MY_AUDIO_PERMISSION = 1

        /**
         * Text-to-speech request codes
         */

        const val TEXT_TO_SPEECH_REQUEST_CODE = 4

        /**
         * Files and Clipboard (intent request codes & other values)
         */
        const val OPEN_DOC_REQUEST_CODE = 1
        const val FILE_SIZE_REQUEST_CODE = 1
        const val FILE_NAME_REQUEST_CODE = 0

        /**
         * Source naming
         */
        const val MIN_RAW_TOKENS_FOR_NAMING = 3
        const val MAX_RAW_TOKENS_FOR_ALERT_MESSAGE = 60 // around two long sentences
        const val AVERAGE_SENTENCE = 25

        /**
         * Session reading settings
         */
        const val MAX_CHAR_DELAY = 400
        const val MIN_CHAR_DELAY = 150

        const val MAX_HIDE_BONUS_LIMIT = 800
        const val MIN_HIDE_BONUS_LIMIT = 400

    }
}