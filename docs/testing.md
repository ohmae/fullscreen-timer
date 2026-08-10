# テスト構成

## 現在の構成

- DI: Hilt
- ローカルテスト: JUnit4、MockK、Truth、Robolectric、kotlinx-coroutines-test
- UIテスト: Compose Testing v2をRobolectric上で実行
- カバレッジ: Kover
- UI実装: XML ViewとJetpack Composeのハイブリッド
- データ保存: DataStore

Main画面のCompose移行に先立ち、以下のテスト基盤と特性テストを追加している。

- Compose Test Ruleから `setContent` し、Semanticsノードを検証できること
- `ClockView` が時間なし／時間ありのミリ秒値を既存の各桁へ変換すること
- 外部Intentから `Mode` と `Command` を安全に復元すること

Compose UIテストを `test` ソースセットで実行する理由は、端末を必要とせず高速かつ安定して状態別UIを検証するためである。また、
`minSdk 26` 向けのDEXでは空白を含むメソッド名を扱えないため、プロジェクトの日本語テスト命名規約もローカルJVMテストで維持する。

## 実行コマンド

ローカルユニットテストとCompose UIテスト:

```shell
./gradlew :app:testDebugUnitTest
```

Androidテストのコンパイル確認:

```shell
./gradlew :app:compileDebugAndroidTestKotlin
```

コードフォーマット確認:

```shell
./gradlew ktlint
```

プロジェクト全体の検証:

```shell
./gradlew build
```

## ベースライン画像

Main画面の移行前画像と挙動仕様は [main-screen-baseline.md](compose-migration/main-screen-baseline.md)
に保存する。現在の画像は端末から手動取得した比較資料であり、自動スクリーンショットテストのgoldenではない。

Compose版Main画面の実装後に、状態を直接注入できるstateless Composableを対象としてCompose
UIテストを追加する。レイアウトの画像差分を自動化する場合は、現在の手動画像を直接goldenとして流用せず、固定された端末設定とフォント設定で新たにgoldenを生成する。

