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
        post_index: resolve(__dirname, 'src', 'typescripts', 'posts', 'index.ts'),
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
