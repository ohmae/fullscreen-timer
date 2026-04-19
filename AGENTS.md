# Fullscreen Timer - AI Agent Context

## プロジェクト概要
- **目的**: 全画面で時計・タイマー・ストップウォッチを表示するアプリ
- **主要機能**:
    - 全画面表示・任意の色を設定・フォント選択

## エージェントへの基本指示
- あなたはエキスパートレベルのAndroidエンジニアとして振る舞ってください
- コードを提案・生成する際は、以下の技術スタックとコーディング規約を必ず遵守してください
- 既存のコードベースのスタイルと設計思想を尊重し、一貫性を保ってください

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
- **コードフォーマッター**: ktlint のルールに準拠してください
- **命名規則**:
    - Googleの[Kotlinスタイルガイド](https://developer.android.com/kotlin/style-guide)に従う
- **特記事項**:
    - 可能な限り不変（Immutable）なデータ構造を利用し、変数は `val` を優先してください
    - マジックナンバーは避け、適切に定数として定義してください

## テスト方針
- **ユニットテスト**: JUnit4/MockK/Truth
- **UIテスト**: Compose Testing
- **テストのルール**: ビジネスロジック追加時には、必ず対応するユニットテストを記述すること

## 禁止事項・避けるべきパターン
- Javaコードの新規利用は禁止、必ずKotlinを使用
- 非推奨 (Deprecated) となったAndroid APIやライブラリは使用しない
- UI層（ComposableやActivity）に直接ビジネスロジックを記述しない
- 複雑なロジックはUI層はもちろん、ViewModelの中にも直接は記述せず、ロジックを単独のクラスとして切り出すこと
