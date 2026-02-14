export interface PostResponse {
  id: string
  title: string
  body: string
  bodyHtml: string
  authorId: string
  authorUsername: string
  createdAt: string
  updatedAt: string
}

export interface PostListResponse {
  posts: PostResponse[]
}

export interface CreatePostRequest {
  title: string
  body: string
}

export interface WhoamiResponse {
  id: number
  externalId: string
  email: string
  displayName: string
  avatarUrl: string | null
}

export interface DevUser {
  username: string
  displayName: string
}

export interface DevUsersResponse {
  users: DevUser[]
}

export interface LoginResponse {
  sessionToken: string
  user: DevUser
}
