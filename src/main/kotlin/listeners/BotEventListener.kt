package listeners

import BotMain
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.user.UserTypingEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

class BotEventListener(val botMain: BotMain) : ListenerAdapter() {
    private var listeningEnabled: Boolean = false
    private val typingMap = HashMap<String, MessageMeta>()

    data class MessageMeta(val userNick: String, val timer: TimerTask, val message: Message)

    private fun handleSentMessage(userId: String, userNick: String?, message: Message?) {
        if (message != null && userNick != null) {
            // If this ends up timing out edit message to say <User> never sent a message...
            val sentTimer = Timer(userNick, false).schedule(10 * 1000) {
                val embedBuilder = EmbedBuilder()
                embedBuilder.setDescription("$userNick never sent a message...")
                message.editMessage(embedBuilder.build()).queue()
            }

            val meta = MessageMeta(userNick, sentTimer, message)

            typingMap[userId] = meta
        }
    }

    private fun deleteMessageLater(message: Message?, milliseconds: Long){
        if (message != null) {
            Timer(message.id, false).schedule(milliseconds) {
                message.delete().queue()
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userId = event.message.author.id

        // Only allow Keiaxx#7052 to summon the bot
        if (userId == "107322097553960960"){
            val messageContent = event.message.contentRaw.toLowerCase()

            if (messageContent == "i wish i knew when daniel was typing"){
                listeningEnabled = true
                event.channel.sendMessage("I've got your back! I'll let you know when daniel is typing : )")
                    .queue { message: Message? ->  deleteMessageLater(message, 10000)}
            }

            if (messageContent == "i do not need to know when daniel is typing"){
                listeningEnabled = false
                event.channel.sendMessage("Ok... I'll stop letting you know when he is typing : (")
                    .queue { message: Message? ->  deleteMessageLater(message, 10000)}
            }
        }

        if (typingMap.containsKey(userId)) {
            val mapValue: MessageMeta? = typingMap[userId]

            if (mapValue != null) {
                mapValue.timer.cancel()

                val embedBuilder = EmbedBuilder()
                embedBuilder.setDescription(
                    "${mapValue.userNick} is done typing, and has sent a message. " +
                            "Please respond to his message."
                )
                mapValue.message.editMessage(embedBuilder.build()).queue()
            }
        }
    }

    override fun onUserTyping(event: UserTypingEvent) {
        println(event.user.name + "  is typing")
        val guild = event.guild

        if(!listeningEnabled) return

        // Only send if user is NotFunstuff#4953
        // otherwise configurable for multiple user ids
        var userIds = arrayOf("122090401011073029")
        if (guild != null && event.user.id in userIds) {
            val guildMember = guild.getMemberById(event.user.id)

            if (guildMember != null) {
                val nickname = if (guildMember.nickname == null) guildMember.effectiveName else guildMember.nickname
                val embedBuilder = EmbedBuilder()
                embedBuilder.setDescription("$nickname is typing...")
                event
                    .channel
                    .sendMessage(embedBuilder.build())
                    .queue { message: Message? -> handleSentMessage(event.user.id, nickname, message) }
            }
        }
    }
}