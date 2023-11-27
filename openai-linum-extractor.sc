//> using scala "3.3.0"
//> using dep "io.cequence::openai-scala-client:0.5.0"
//> using dep "com.lihaoyi::os-lib:0.9.2"
import io.cequence.openaiscala.domain.ChatRole
import io.cequence.openaiscala.domain.MessageSpec

import scala.concurrent.Future
import akka.stream.Materializer
import akka.actor.ActorSystem
import java.io.File
import io.cequence.openaiscala._
import io.cequence.openaiscala.service._
import io.cequence.openaiscala.domain.response._
import io.cequence.openaiscala.domain.settings._
import io.cequence.openaiscala.service.ws.Timeouts

object LinumExtractor {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val materializer: Materializer = Materializer(ActorSystem())
  def main(args: Array[String]): Unit = {
    val service = OpenAIServiceFactory(
      apiKey = sys.env("OPENAI_SCALA_CLIENT_API_KEY"),
      orgId = None,
      timeouts = Some(
        Timeouts(
          requestTimeout = Some(3600 * 1000),
          readTimeout = Some(3600 * 1000)
        )
      )
    )
    val contentFilePath = args(0)
    val file = os.read(os.pwd / contentFilePath)
    val phrase = args(1)
    val prompt = s"""
あなたは検索エンジンです。まず僕が文書を入力します。その次に、その文書の一部を要約した文を入力します。あなたは、その文が要約している箇所をハイライトするために、文書の対応する行数を返してください。

例:
記事:
今日は冬が始まったようで、とても寒い朝だった。
昼はスターバックスに行ったけれど、けっこう混んでいた。
夜は鍋を作って食べることにした。うまく作れておいしかった。

文:
スタバが混んでいた

回答: 2

回答の形式例:
- 42
- 50,61
- 1-3
- 50-61,100-110
- N/A

実際の問題:
記事:
${file}

文:
${phrase}

回答:
""".stripMargin
    val setting =
      CreateChatCompletionSettings("gpt-4-1106-preview")
    val msgs = Seq(MessageSpec(ChatRole.System, prompt))
    val completion = service.createChatCompletion(msgs, setting)

    completion.andThen { result =>
      println(s"Line: ${result.get.choices.head.message.content}")
      materializer.system.terminate()
      service.close()
    }
  }
}

LinumExtractor.main(args)
