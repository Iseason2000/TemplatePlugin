package top.iseason.bukkit.bukkittemplate.command

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

class TypeParam<T : Any>(
    val type: KClass<T>,
    var errorMessage: (String) -> String = { "Param ${it}is not exist" },
    val onCast: TypeParam<*>.(String) -> T?
) {
    companion object {
        val typeParams = mutableMapOf<KClass<*>, TypeParam<*>>()

        fun addTypeParam(typeParam: TypeParam<*>) {
            typeParams[typeParam.type] = typeParam
        }

        fun removeType(type: KClass<*>) = typeParams.remove(type)

        // 可选参数
        inline fun <reified T> getOptionalParam(paramStr: String): T? {
            val typeParam = typeParams[T::class] ?: throw ParmaException("Param Type is not exist!")
            return typeParam.onCast(typeParam, paramStr) as? T
        }

        //必须的参数
        inline fun <reified T> getParam(paramStr: String): T {
            val typeParam = typeParams[T::class] ?: throw ParmaException("Param Type is not exist!")
            return typeParam.onCast(typeParam, paramStr) as? T ?: throw ParmaException(paramStr, typeParam)
        }

        init {
            setDefaultParams()
        }

        private fun setDefaultParams() {
            TypeParam(Player::class, errorMessage = { "&7玩家 &c${it} &7不存在!" }) {
                var player = Bukkit.getPlayer(it)
                if (player == null && it.length == 36) {
                    player = try {
                        Bukkit.getPlayer(UUID.fromString(it))
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                player
            }.register()
            TypeParam(OfflinePlayer::class, errorMessage = { "&7玩家 &c${it} &7不存在!" }) {
                var player: OfflinePlayer? = Bukkit.getOfflinePlayer(it)
                if (!player!!.hasPlayedBefore()) {
                    player = try {
                        val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(it))
                        if (offlinePlayer.hasPlayedBefore()) offlinePlayer else null
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                player
            }.register()
            TypeParam(Int::class, errorMessage = { "&c${it} &7不是一个有效的整数" }) {
                try {
                    it.toInt()
                } catch (e: NumberFormatException) {
                    null
                }
            }.register()
            TypeParam(Double::class, errorMessage = { "&c${it} &7不是一个有效的小数" }) {
                try {
                    it.toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }.register()
            TypeParam(String::class) { it }.register()
        }
    }
}

fun TypeParam<*>.register() {
    TypeParam.addTypeParam(this)
}