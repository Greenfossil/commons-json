package experiments.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.JsonNode

class JsonNodeSuite extends munit.FunSuite {

  test("JsonNode.at should return MissingNode for missing field using Jackson API") {
    val mapper = new ObjectMapper()
    val node: JsonNode = mapper.readTree("{}")
    val result: JsonNode = node.at("/name")

    // JsonNode.at should return a MissingNode (not null) when the path is missing
    assert(result != null, s"JsonNode.at returned null")
    assert(result.isMissingNode, s"Expected MissingNode but got: $result")
    assert(result.isInstanceOf[MissingNode])
    assertEquals(result, MissingNode.getInstance())
  }

}
