package com.vertyll.snaptale.profile;

import java.util.List;

import com.vertyll.snaptale.follow.FollowCounts;
import com.vertyll.snaptale.post.PostCard;
import com.vertyll.snaptale.user.UserProfile;

record ProfileResponse(UserProfile user, FollowCounts follows, boolean followedByMe, List<PostCard> posts) {
}
