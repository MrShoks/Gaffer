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
package gaffer.serialisation.simple;

import gaffer.commonutil.CommonConstants;
import gaffer.exception.SerialisationException;
import gaffer.serialisation.Serialisation;
import java.io.UnsupportedEncodingException;

/**
 * @deprecated please use {@link gaffer.serialisation.implementation.IntegerSerialiser}
 */
@Deprecated
public class IntegerSerialiser implements Serialisation {

    private static final long serialVersionUID = 5647756843689779437L;

    @Override
    public boolean canHandle(final Class clazz) {
        return Integer.class.equals(clazz);
    }

    @Override
    public byte[] serialise(final Object object) throws SerialisationException {
        Integer value = (Integer) object;
        try {
            return value.toString().getBytes(CommonConstants.ISO_8859_1_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SerialisationException(e.getMessage(), e);
        }
    }

    @Override
    public Object deserialise(final byte[] bytes) throws SerialisationException {
        try {
            return Integer.parseInt(new String(bytes, CommonConstants.ISO_8859_1_ENCODING).trim());
        } catch (NumberFormatException | UnsupportedEncodingException e) {
            throw new SerialisationException(e.getMessage(), e);
        }
    }
}
