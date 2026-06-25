import {defineConfig} from 'rolldown'

export default defineConfig({
    input: 'src/extension.ts',
    output: {
        file: 'dist/extension.js',
        format: 'cjs',
        sourcemap: true
    },
    platform: 'node',
    external: ['vscode']
})
