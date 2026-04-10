# DeckWave Remote Android

Este e um app Android real para abrir o modo remoto do DeckWave em tela cheia.

## O que ele faz

- splash screen dedicada
- barra nativa com `Abas` e `Endereco`
- tema escuro
- `WebView` em tela cheia para o controle remoto
- salva o endereco do servidor no aparelho
- mostra tela de erro quando nao encontra o PC na rede

## Abrindo no Android Studio

1. Abra a pasta `android-app`
2. Deixe o Android Studio sincronizar o Gradle
3. Rode no celular ou emulador
4. Se necessario, toque em `Endereco` e use algo como:

```text
http://192.168.3.9:3939/?mode=remote
```

## Gerar APK sem Android Studio

Sim, da para gerar sem Android Studio, mas voce precisa ter o Android SDK e o Gradle configurados na maquina.

Exemplo por linha de comando:

```powershell
cd android-app
gradle assembleDebug
```

Se voce usar Gradle Wrapper, o ideal seria:

```powershell
cd android-app
.\gradlew.bat assembleDebug
```

## Observacao importante

Neste projeto eu deixei o app Android pronto, mas nao consegui gerar o `.apk` daqui porque este ambiente nao mostrou o Android SDK/Gradle completos para build final.

Se quiser, no proximo passo eu posso preparar tambem:

- `gradlew.bat` e arquivos do wrapper, se voce ja tiver Gradle
- assinatura de release
- nome final do pacote
- splash ainda mais premium
