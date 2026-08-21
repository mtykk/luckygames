package io.github.mtykk.luckygames;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class LuckyBlockTypeArgument implements CustomArgumentType.Converted<LuckyBlockTypes,String>{
    private static final DynamicCommandExceptionType ERROR_INVALID_TYPE = new DynamicCommandExceptionType(type -> {
        return MessageComponentSerializer.message().serialize(Component.text(type + " is not a valid lucky block type"));
    });

    @Override
    public LuckyBlockTypes convert(String nativeType) throws CommandSyntaxException {
        try{
            return LuckyBlockTypes.valueOf(nativeType.toUpperCase(Locale.ROOT));
        }catch(IllegalArgumentException ignored){
            throw ERROR_INVALID_TYPE.create(nativeType);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
        for (LuckyBlockTypes type: LuckyBlockTypes.values()){
            String name = type.toString();

            if (name.startsWith(builder.getRemainingLowerCase())){
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType(){
        return StringArgumentType.word();
    }
}