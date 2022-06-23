import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: resolve(__dirname, '..', 'public', 'javascripts'),
    rollupOptions: {
      input: {
        posts_index: resolve(__dirname, 'src', 'typescripts', 'posts', 'index.ts'),
        posts_detail: resolve(__dirname, 'src', 'typescripts', 'posts', 'detail.ts'),
        ranking_index: resolve(__dirname, 'src', 'typescripts', 'ranking', 'index.ts'),
        users_detail: resolve(__dirname, 'src', 'typescripts', 'users', 'detail.ts'),
        users_edit: resolve(__dirname, 'src', 'typescripts', 'users', 'edit.ts'),
        users_signIn: resolve(__dirname, 'src', 'typescripts', 'users', 'signIn.ts'),
        main: resolve(__dirname, 'src', 'typescripts', 'main.ts'),
      },
      output: {
        entryFileNames: '[name].js',
        chunkFileNames: '[name].js',
        assetFileNames: '[name][extname]',
      }
    }
  }
})
