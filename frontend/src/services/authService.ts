import axios from 'axios';
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '../types/auth';

const AUTH_TOKEN_KEY = 'auth_token';
const AUTH_USER_KEY = 'auth_user';

/**
 * Authentication service for handling login, register, and logout
 */
class AuthService {
    /**
     * Register a new user
     */
    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await axios.post<AuthResponse>('/api/auth/register', data);
        this.setAuthData(response.data);
        return response.data;
    }

    /**
     * Login with username/email and password
     */
    async login(data: LoginRequest): Promise<AuthResponse> {
        const response = await axios.post<AuthResponse>('/api/auth/login', data);
        this.setAuthData(response.data);
        return response.data;
    }

    /**
     * Logout the current user
     */
    logout(): void {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        localStorage.removeItem(AUTH_USER_KEY);
    }

    /**
     * Get the current auth token
     */
    getToken(): string | null {
        return localStorage.getItem(AUTH_TOKEN_KEY);
    }

    /**
     * Get the current user
     */
    getUser(): User | null {
        const userStr = localStorage.getItem(AUTH_USER_KEY);
        if (!userStr) return null;

        try {
            return JSON.parse(userStr);
        } catch {
            return null;
        }
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated(): boolean {
        return this.getToken() !== null;
    }

    /**
     * Store auth data in localStorage
     */
    private setAuthData(data: AuthResponse): void {
        localStorage.setItem(AUTH_TOKEN_KEY, data.token);
        localStorage.setItem(AUTH_USER_KEY, JSON.stringify({
            username: data.username,
            email: data.email
        }));
    }
}

export const authService = new AuthService();
