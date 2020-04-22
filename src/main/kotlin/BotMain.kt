import listeners.BotEventListener
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDABuilder

class BotMain(val botToken: String) {
    fun startBot() {
        println("Starting bot salad")

        val jdab = JDABuilder(AccountType.BOT)
            .setToken(botToken)
            .setAutoReconnect(true)
            .addEventListeners(BotEventListener(this))

        jdab.build()
    }
}