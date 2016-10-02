package karl.codes.hal

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
import karl.codes.hal.example.RootMixin
import karl.codes.hal.example.Simple
import spock.lang.Specification
import spock.lang.Unroll

import static karl.codes.Groovy.*
import static org.hamcrest.Matchers.instanceOf
import static spock.util.matcher.HamcrestSupport.that

/**
 * Created by karl on 8/22/16.
 */
class NewRootWrapTest extends Specification {
    static standardJson = new ObjectMapper()
    static mixinJson = new ObjectMapper()
            .addMixIn(RootInputWrap.class, RootMixin.class)
            .addMixIn(RootOutputWrap.class, RootMixin.class)
    static injectingJson = new ObjectMapper(null, new WrappingSerializerProvider(), new WrappingDeserializationContext(BeanDeserializerFactory.instance))
    static injMixJson = new ObjectMapper(null, new WrappingSerializerProvider(), new WrappingDeserializationContext(BeanDeserializerFactory.instance))
            .addMixIn(RootInputWrap.class, RootMixin.class)
            .addMixIn(RootOutputWrap.class, RootMixin.class)

    static directType = new TypeReference<Simple>(){}
    static wrappedType = new TypeReference<RootInputWrap<Simple>>() {}


    @Unroll('root wrap case: #desc')
    def testRootWrap() {
        when:
        if(skip) {
            return;
        }
        def ser = json.readValue(data, type)
        // unwrapping the object shape
        def doc = objShape.inject(ser,{ a, v -> a[v]})
        String txt = json.writeValueAsString(ser)
        JsonNode out = json.readTree(txt)
        // unwrapping the raw tree shape
        JsonNode outDoc = rawShape.inject(out,{ a, v -> a[v]})


        then:
        that doc, instanceOf(Simple)
        outDoc[k].asText() == v

        where:
        desc | skip | json | type | rawShape | objShape | data | k | v

        'a wrapped type provides the desired object shape' | false |
        standardJson | wrappedType | []    | ['body'] |
            js([
                key: 'key',
                value: 'value'
            ]) | 'key' | 'key'

        'a mixin only satisfies the rawShape' | false |
        mixinJson | wrappedType | ['body'] | ['body'] |
            js([
                body: [
                    key: 'key',
                    value: 'value'
                ]
            ]) | 'key' | 'key'

        'wrapping serializer and direct type has no side effects' | false |
        injectingJson | directType | [] | [] |
            js([
                key: 'key',
                value: 'value'
            ]) | 'key' | 'key'

        'wrapping serializer and mixin provides the desired shape' | false |
        injMixJson | directType| ['body'] | [] |
            js([
                body: [
                    key: 'key',
                    value: 'value'
                ]
            ]) | 'key' | 'key'
    }
}
