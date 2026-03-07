import { WhoamiResponse, DevUsersResponse, LoginResponse } from '../types/api'

export async function fetchWhoami(): Promise<WhoamiResponse | null> {
  const res = await fetch('/api/v1/whoami')
  if (res.status === 401) return null
  if (!res.ok) throw new Error('Failed to check auth status')
  return res.json()
}

export async function fetchDevUsers(): Promise<DevUsersResponse> {
  const res = await fetch('/api/v1/auth/dev-users')
  if (!res.ok) throw new Error('Failed to fetch dev users')
  return res.json()
}

export async function login(username: string): Promise<LoginResponse> {
  const res = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username }),
  })
  if (!res.ok) throw new Error('Login failed')
  return res.json()
}

export async function logout(): Promise<void> {
  await fetch('/logout', { method: 'POST', redirect: 'manual' })
}
