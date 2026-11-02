# FireAuth Perfil — Android (Kotlin)

App com tela de login personalizada usando Firebase Authentication (e-mail/senha).

## Funcionalidades
| Requisito | Onde está | Método Firebase |
|---|---|---|
| Logar | `LoginActivity.logar()` | `signInWithEmailAndPassword` |
| Criar usuário | `CadastroActivity.criarUsuario()` | `createUserWithEmailAndPassword` |
| E-mail de redefinição de senha | `LoginActivity.redefinirSenha()` ("Esqueci minha senha") | `sendPasswordResetEmail` |
| Excluir a própria conta após logar | `PerfilActivity.excluirConta()` | `currentUser.delete()` (+ `reauthenticate` se pedir login recente) |

Extra: botão **Sair** (`signOut`) e login automático se o usuário já estiver autenticado.

## Como rodar
1. Abra a pasta `FireAuthPerfil` no Android Studio e aceite o Sync do Gradle.
2. O `app/google-services.json` já está incluído (projeto Firebase `appkotlinfatec-44e5e`), por isso o `applicationId` é `br.edu.fatecpg.appkotlinfatec`.
3. No console do Firebase, confira em **Authentication → Método de login** se **E-mail/senha** está ativado.
4. Rode no emulador ou no celular.

## Teste rápido
1. "Criar nova conta" → cadastre um e-mail real → cai no Perfil.
2. "Sair" → faça login de novo.
3. "Esqueci minha senha" → confira o e-mail (olhe o spam).
4. No Perfil → "Excluir minha conta" → o usuário some em Authentication → Users.
