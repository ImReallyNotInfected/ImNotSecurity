package org.imnotasecurity.internal.Auth;

import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.dialog.*;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.Properties.AbstractProperty;

import java.util.List;

public class AuthGUIs {
    public static Dialog getRegisterGUI() {
        AbstractProperty property = ImNotSecurity.getProperty();

        var passwordField = new DialogInput.Text("password_register",200, Component.text(
                switch (property.getLanguage()) {
                    case VIETNAMESE -> "Xin Hãy Nhập Mật Khẩu";
                    case null, default -> "Please Enter Password : ";
                }
        ),true,"",20, null);
        var confirmPasswordField = new DialogInput.Text("confirm_password_register",200, Component.text(
                switch (property.getLanguage()) {
                    case VIETNAMESE -> "Xin Hãy Nhập Lại Mật Khẩu";
                    case null, default -> "Please Confirm Your Password : ";
                }
        ),true,"",20, null);

        var registerButton = new DialogActionButton(Component.text("Register"),null,175,new DialogAction.DynamicCustom(Key.key("register"), CompoundBinaryTag.builder().build()));
        var meta = new DialogMetadata(Component.text("Authentication Required"),Component.empty(),false,false,
                DialogAfterAction.CLOSE, List.of(new DialogBody.PlainMessage(Component.empty(),75)),
                List.of(passwordField, confirmPasswordField));
        var e = new Dialog.MultiAction(meta,List.of(registerButton),null,1);

        return e;
    }

    public static Dialog getLoginGUI() {
        AbstractProperty property = ImNotSecurity.getProperty();

        var passwordField = new DialogInput.Text("password_login",200, Component.text(
                switch (property.getLanguage()) {
                    case VIETNAMESE -> "Xin Hãy Nhập Mật Khẩu Đăng Ký";
                    case null, default -> "Please Enter Login Password : ";
                }
        ),true,"",20, null);

        var registerButton = new DialogActionButton(Component.text("Register"),null,175,new DialogAction.DynamicCustom(Key.key("login"), CompoundBinaryTag.builder().build()));
        var meta = new DialogMetadata(Component.text("Authentication Login Required"),Component.empty(),false,false,
                DialogAfterAction.CLOSE, List.of(new DialogBody.PlainMessage(Component.empty(),75)),
                List.of(passwordField));
        var e = new Dialog.MultiAction(meta,List.of(registerButton),null,1);
        return e;
    }
}
