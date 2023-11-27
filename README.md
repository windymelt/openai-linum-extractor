# 記事中から該当行を取り出すプロンプト

preprocess: `nl -n ln -b a blog.txt > bloglinum.txt`

run: `OPENAI_SCALA_CLIENT_API_KEY="*****" scala-cli openai-linum-extractor.sc -- bloglinum.txt "探したい内容"`
