package com.dartsmatcher.dartsmatcherapi.features.user;

import com.dartsmatcher.dartsmatcherapi.validators.nowhitespace.NoWhitespace;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
@TypeAlias("User")
public class User {

	@MongoId
	private ObjectId id;

	@NotBlank(groups = {Create.class, Update.class})
	@Length(groups = {Create.class, Update.class}, min = 4, max = 24)
	@NoWhitespace(groups = {Create.class, Update.class})
//	@ValidMatchPlayerIds(groups = {Create.class, Update.class}) // TODO: FIX CHANGE VALIDATOR TYPE TO STRING AND IN MATCHPLAYER DO ARRAYLIST<@ValidMatchPlayerId String> PLAYERS
	private String userName;

	@NotBlank(groups = {Create.class, Update.class})
	private String firstName;

	@NotBlank(groups = {Create.class, Update.class})
	private String lastName;

	@NotBlank(groups = {Create.class, Update.class})
	@Email(groups = {Create.class, Update.class})
	private String email;

	private ArrayList<ObjectId> friends;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(groups = {Create.class, Update.class})
	@Length(groups = {Create.class, Update.class}, min = 4, max = 24)
	private String password;

	public interface Create {
	}

	public interface Update {
	}

}