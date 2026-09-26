package com.vertyll.snaptale.post;

import java.util.List;

record PostDetails(PostCard post, List<CommentView> comments, List<Long> authorPostIds) {
}
