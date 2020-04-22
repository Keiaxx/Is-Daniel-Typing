var botToken: String = System.getenv("token") ?: ""
fun main(args: Array<String>) {
    val bot = BotMain(botToken)
    bot.startBot()
}
