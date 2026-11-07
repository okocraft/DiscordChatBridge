![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/DiscordChatBridge)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/DiscordChatBridge/gradle.yml?branch=master)
![GitHub](https://img.shields.io/github/license/okocraft/DiscordChatBridge)

# DiscordChatBridge

LunaChat と Discord をチャット連携するプラグイン。

## Requirements

- Java 25+
- Paper / Velocity
- (Optional) [LunaChat 3.0.8+](https://github.com/ucchyocean/LunaChat/releases/tag/v3.0.8)
  - Paper では任意、Velocity では必須です。
  - Paper で LunaChat がない場合は、通常のチャットを連携します。
- (Optional) LuckPerms: 初参加通知（`message-sending-setting.first-join`）に使用します。

## Discord Bot の準備

1. Discord Developer Portal で Bot を作成し、Privileged Gateway Intents の **Server Members Intent** と **Message Content Intent** を有効にします。
2. 連携するチャンネルで、Bot に次の権限を与えます。
   - チャンネルを見る
   - メッセージを送信
   - メッセージ履歴を読む
   - リアクションを追加
3. Bot のトークンは `config.yml` の `discord.token` にのみ記載します。公開リポジトリ、Issue、ログなどに貼り付けないでください。

## Usage

1. `plugins` ディレクトリ内に配置し、サーバーを起動する。
   - トークンが未設定のため、初回はプラグインが無効になります。
2. 作成された `config.yml` にトークンとチャンネル ID を設定し、サーバーを再起動する。
3. 以降の設定変更は `/dcbreload` で反映できる。

### チャンネルの設定

| 設定 | 用途 |
| --- | --- |
| `system-channel` | 参加・退出・初参加・サーバー移動（Velocity）の通知と `!playerlist` の応答先。LunaChat がない Paper では、ゲーム内チャットの送信先も兼ねます。 |
| `channels` | `<LunaChat のチャンネル名>: <Discord のチャンネル ID>` の形式で、双方向に連携します。LunaChat がない Paper では `global` だけを使い、Discord からゲーム内への方向に連携します。 |

LunaChat がない Paper でチャットを双方向に連携するには、`system-channel` と `channels.global` に同じチャンネル ID を設定してください。

### `/dcbreload`

- 権限: `discordchatbridge.reload`
- `config.yml` と `format.yml` を再読み込みします。
- 設定に誤りがある場合は、現在の設定と接続をそのまま維持します。
- Discord には、トークンを変更した場合と接続が停止している場合だけ再接続します。
- 接続の成否は、コマンドの応答ではなくコンソールに出力されます。

## Build

Java 25 を使用し、`./gradlew clean build` でビルドします。
配布用 JAR は `build/libs/DiscordChatBridge-3.0-SNAPSHOT.jar` で、Paper と Velocity で共通の JAR です。
`-plain.jar` は依存ライブラリを含まないため、サーバーには配置しないでください。

実サーバーでの確認手順は [TESTING.md](TESTING.md) を参照してください。
開発の規約は [okocraft/docs](https://github.com/okocraft/docs) に従います。

## License

このプロジェクトは GPL-3.0 のもとで公開しています。詳しくは [ライセンスファイル](LICENSE) をお読みください。

This project is under the GPL-3.0 license. Please see [LICENSE](LICENSE) for more info.

Copyright © 2020-2026, Okocraft
