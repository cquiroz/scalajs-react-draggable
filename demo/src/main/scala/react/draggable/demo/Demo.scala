package react.draggable.demo

import scala.scalajs.js
import scala.scalajs.js.annotation._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import react.virtualized._
import Data.DataRow

object TableDemo {

  def datum(data: List[DataRow])(i: Int) = data(i % data.length)
  def rowheight(data: List[DataRow])(i: Int) = datum(data)(i).size

  final case class Props(useDynamicRowHeight: Boolean, sortBy: String, s: Size)
  final case class State(sortDirection: SortDirection, data: List[DataRow])

  def headerRenderer(sortBy: String)(columnData: DataRow, dataKey: String, disableSort: Option[Boolean], label: VdomNode, sortByParam: Option[String], sortDirection: SortDirection): VdomNode =
    <.div("Full Name", SortIndicator(SortDirection.ASC).when(sortBy == dataKey))

  def rowClassName(i: Int): String = i match {
    case x if x < 0      => "headerRow"
    case x if x % 2 == 0 => "evenRow"
    case _               => "oddRow"
  }

  val component = ScalaComponent.builder[Props]("TableDemo")
    .initialState(State(SortDirection.ASC, Data.generateRandomList))
    .renderPS{($, props, state) =>
      def sort(index: String, sortDirection: SortDirection): Callback = {
        val sorted = state.data.sortBy(_.index)
        $.setState(state.copy(data = if (sortDirection == SortDirection.ASC) sorted else sorted.reverse, sortDirection = sortDirection))
      }
      val columns = List(
        Column(Column.props(60, "index", label = "Index", disableSort = false)),
        Column(Column.props(90, "name", disableSort = false, headerRenderer = headerRenderer(props.sortBy))),
        Column(Column.props(210, "random", disableSort = true, className = "exampleColumn", label = "The description label is so long it will be truncated", flexGrow = 1, cellRenderer = (cellData: DataRow, _: js.Any, _: String, _: js.Any, _: Int) => cellData.toString))
      )
      val t = Table(
        Table.props(
          disableHeader = false,
          noRowsRenderer = () => <.div(^.cls := "noRows", "No rows"),
          overscanRowCount = 10,
          rowClassName = rowClassName _,
          height = 270,
          rowCount = 1000,
          rowHeight = if (props.useDynamicRowHeight) rowheight(state.data) _ else 40,
          onRowClick = x => Callback.log(x),
          onScroll = (c, s, t) => Callback.log(s"$c $s $t"),
          width = props.s.width.toInt,
          rowGetter = datum(state.data),
          headerClassName = "headerColumn",
          sort = sort _,
          sortBy = props.sortBy,
          sortDirection = state.sortDirection,
          headerHeight = 30), columns: _*)
      t.mapMounted(_.raw.scrollToRow(20))
      t
    }
    .build

  def apply(p: Props) = component(p)
}

@JSExportTopLevel("Demo")
object Demo {
  @JSImport("react-virtualized/styles.css", JSImport.Default)
  @js.native
  object ReactVirtualizedStyles extends js.Object

  @JSExport
  def main(): Unit = {
    // needed to load the styles
    ReactVirtualizedStyles
    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }
    val tableF = (s: Size) =>
      TableDemo(TableDemo.Props(true, "index", s)).vdomElement

    AutoSizer(AutoSizer.props(tableF, disableHeight = true)).renderIntoDOM(container)
    ()
  }
}
