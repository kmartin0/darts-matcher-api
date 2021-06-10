package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import com.dartsmatcher.dartsmatcherapi.features.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDetails {
	private ArrayList<User> friendsDetails;
}
