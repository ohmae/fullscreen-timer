# Fullscreen Timer

## プロジェクト概要
- **目的**: 全画面で時計・タイマー・ストップウォッチを表示するアプリ
- **主要機能**: 全画面表示・任意の色を設定・フォント選択

## エージェントへの基本指示
- Google推奨のAndroid開発のベストプラクティスに従うこと
- コードを提案・生成する際は、以下の技術スタックとコーディング規約を必ず遵守すること
- 既存のコードベースのスタイルと設計思想を尊重し、一貫性を保つこと

## 技術スタック
- **言語**: Kotlin
- **SDK**: `minSdk` = 26, `targetSdk` = 36
- **UIツールキット**: XML (View) / Compose へ移行中
- **DI**: Hilt
- **非同期処理**: Kotlin Coroutines & Flow
- **ローカルデータ保存**: DataStore

## アーキテクチャと設計方針
- **アーキテクチャパターン**: MVVM
- **状態管理**: UIの状態はViewModel内で `StateFlow` を用いて管理し、Activity/Fragment/Composableから監視すること

## コーディング規約
- **コードフォーマッター**: ktlint
- Googleの[Kotlinスタイルガイド](https://developer.android.com/kotlin/style-guide)に従う
- スコープ関数は可読性を優先して選択、`apply`と`also`の可読性が同等であれば、`also`を優先すること

## テスト方針
- **ユニットテスト**: JUnit4/MockK/Truth
- **UIテスト**: Compose Testing
- **テストのルール**: ビジネスロジック追加時には、必ず対応するユニットテストを記述すること

## 禁止事項・避けるべきパターン
- Javaコードの新規利用は禁止、必ずKotlinを使用
- 非推奨 (Deprecated) となったAndroid APIやライブラリは使用しない
- UI層（ComposableやActivity）に直接ビジネスロジックを記述しない
- 複雑なロジックはUI層はもちろん、ViewModelの中にも直接は記述せず、ロジックを単独のクラスとして切り出すこと
