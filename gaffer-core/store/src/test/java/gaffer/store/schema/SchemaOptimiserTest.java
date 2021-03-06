/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gaffer.store.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import gaffer.commonutil.TestGroups;
import gaffer.commonutil.TestPropertyNames;
import gaffer.serialisation.implementation.JavaSerialiser;
import gaffer.serialisation.implementation.SerialisationFactory;
import gaffer.serialisation.implementation.StringSerialiser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.Serializable;

public class SchemaOptimiserTest {
    private TypeDefinition stringType;
    private TypeDefinition intType;
    private Schema schema;

    @Before
    public void setup() {
        stringType = new TypeDefinition.Builder()
                .clazz(String.class)
                .build();
        intType = new TypeDefinition.Builder()
                .clazz(Integer.class)
                .build();
        schema = new Schema.Builder()
                .entity(TestGroups.ENTITY, new SchemaEntityDefinition.Builder()
                        .vertex("string")
                        .property(TestPropertyNames.PROP_1, "string")
                        .property(TestPropertyNames.INT, "int")
                        .groupBy(TestPropertyNames.PROP_1)
                        .build())
                .edge(TestGroups.EDGE_2, new SchemaEdgeDefinition.Builder()
                        .source("string")
                        .destination("string")
                        .property(TestPropertyNames.PROP_1, "string")
                        .property(TestPropertyNames.INT, "int")
                        .build())
                .type("string", stringType)
                .type("int", intType)
                .type("unusedType", new TypeDefinition.Builder()
                        .clazz(Object.class)
                        .build())
                .build();
    }

    @Test
    public void shouldRemoveUnusedTypes() {
        //Given
        final SchemaOptimiser optimiser = new SchemaOptimiser();
        final boolean isOrdered = true;

        // When
        optimiser.optimise(schema, isOrdered);

        // Then
        assertEquals(2, schema.getTypes().size());
        assertEquals(stringType, schema.getType("string"));
        assertEquals(intType, schema.getType("int"));
    }

    @Test
    public void shouldAddDefaultSerialisers() {
        //Given
        final SerialisationFactory serialisationFactory = mock(SerialisationFactory.class);
        final SchemaOptimiser optimiser = new SchemaOptimiser(serialisationFactory);
        final boolean isOrdered = true;

        final StringSerialiser stringSerialiser = mock(StringSerialiser.class);
        final StringSerialiser intSerialiser = mock(StringSerialiser.class);
        final JavaSerialiser javaSerialiser = mock(JavaSerialiser.class);
        given(serialisationFactory.getSerialiser(String.class, true)).willReturn(stringSerialiser);
        given(serialisationFactory.getSerialiser(Integer.class, false)).willReturn(intSerialiser);
        given(serialisationFactory.getSerialiser(Serializable.class, true)).willReturn(javaSerialiser);
        given(javaSerialiser.canHandle(Mockito.any(Class.class))).willReturn(true);

        schema.addEntity(TestGroups.ENTITY_2, new SchemaEntityDefinition.Builder()
                .vertex(Serializable.class)
                .build());

        // When
        optimiser.optimise(schema, isOrdered);

        // Then
        assertSame(stringSerialiser, stringType.getSerialiser());
        assertSame(intSerialiser, intType.getSerialiser());
        assertSame(javaSerialiser, schema.getVertexSerialiser());
        verify(serialisationFactory, never()).getSerialiser(String.class, false);
        verify(serialisationFactory, never()).getSerialiser(Serializable.class, false);
    }

    @Test
    public void shouldThrowExceptionIfDefaultVertexSerialiserCouldNotBeFound() {
        //Given
        final SchemaOptimiser optimiser = new SchemaOptimiser();
        final boolean isOrdered = true;

        // Add a new entity with vertex that can't be serialised
        schema.addEntity(TestGroups.ENTITY_2, new SchemaEntityDefinition.Builder()
                .vertex(Object.class)
                .build());

        // When / Then
        try {
            optimiser.optimise(schema, isOrdered);
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }
}