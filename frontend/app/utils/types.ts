export interface UserSummary {
  id: number;
  name: string;
  avatarUrl: string | null;
}

export interface UserProfile {
  id: number;
  name: string;
  bio: string | null;
  avatarUrl: string | null;
  joinedAt: string;
}

export interface Me {
  id: number;
  name: string;
  email: string;
  bio: string | null;
  avatarUrl: string | null;
  emailVerified: boolean;
}

export interface PostCard {
  id: number;
  text: string;
  videoUrl: string;
  createdAt: string;
  author: UserSummary;
  likeCount: number;
  commentCount: number;
  likedByMe: boolean;
}

export interface CommentView {
  id: number;
  text: string;
  createdAt: string;
  author: UserSummary;
}

export interface PostDetails {
  post: PostCard;
  comments: CommentView[];
  authorPostIds: number[];
}

export interface FeedPage {
  posts: PostCard[];
  hasMore: boolean;
}

export interface ProfileResponse {
  user: UserProfile;
  follows: { followers: number; following: number };
  followedByMe: boolean;
  posts: PostCard[];
}

export type MessageArgs = Record<string, string | number>;

export interface Message {
  code: string;
  args: MessageArgs;
}

export interface ProblemDetail {
  status: number;
  code: string;
  args: MessageArgs;
  errors?: Record<string, Message>;
}
