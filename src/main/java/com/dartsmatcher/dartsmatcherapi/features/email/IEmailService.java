package com.dartsmatcher.dartsmatcherapi.features.email;

import com.dartsmatcher.dartsmatcherapi.features.user.User;
import com.dartsmatcher.dartsmatcherapi.features.user.password.reset.PasswordToken;

import javax.mail.MessagingException;


public interface IEmailService {
	void sendForgotPasswordEmail(User user, PasswordToken passwordToken) throws MessagingException;
}
