import type { PostCard } from "~/utils/types";

export function useLike() {
  const api = useApi();
  const { whenSignedIn } = useOverlays();

  function toggle(post: PostCard): void {
    whenSignedIn(async () => {
      const liked = post.likedByMe;
      post.likedByMe = !liked;
      post.likeCount += liked ? -1 : 1;
      try {
        await (liked ? api.del(`/api/posts/${post.id}/like`) : api.put(`/api/posts/${post.id}/like`));
      } catch {
        post.likedByMe = liked;
        post.likeCount += liked ? 1 : -1;
      }
    });
  }

  return { toggle };
}
