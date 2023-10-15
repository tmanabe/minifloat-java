import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Safetensors {
    static class TensorMetadata {
        String dtype;
        List<Integer> shape;
        AbstractMap.SimpleEntry<Integer, Integer> dataOffsets;
    }

    Map<String, TensorMetadata> header;
    ByteBuffer byteBuffer;

    static Safetensors safe_open(File file) throws IOException {
        DataInputStream dataInputStream;
        {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            dataInputStream = new DataInputStream(bufferedInputStream);
        }

        long headerSize;
        {
            byte[] littleEndianBytesHeaderSize = new byte[8];
            int read = dataInputStream.read(littleEndianBytesHeaderSize);
            assert 8 == read;
            headerSize = ByteBuffer.wrap(littleEndianBytesHeaderSize).order(ByteOrder.LITTLE_ENDIAN).getLong();
        }

        String stringHeader;
        {
            assert headerSize <= Integer.MAX_VALUE;
            byte[] bytesHeader = new byte[(int) headerSize];
            int read = dataInputStream.read(bytesHeader);
            assert headerSize == read;
            stringHeader = new String(bytesHeader, StandardCharsets.UTF_8);
        }

        Safetensors safetensors = new Safetensors();

        int bufferSize = 0;
        {
            Object objectHeader;
            try {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
                objectHeader = scriptEngine.eval("var varHeader = " + stringHeader + "; varHeader;");
            } catch (ScriptException e) {
                throw new IOException(e);
            }

            safetensors.header = new HashMap<>();
            for (Object object : ((Map) objectHeader).entrySet()) {
                Map.Entry entry = (Map.Entry) object;
                String tensorName = entry.getKey().toString();
                if (tensorName.equals("__metadata__")) {
                    continue;
                }
                TensorMetadata tensorMetadata = new TensorMetadata();
                safetensors.header.put(tensorName, tensorMetadata);

                Map map = (Map) entry.getValue();
                tensorMetadata.dtype = map.get("dtype").toString();
                {
                    Map m = (Map) map.get("shape");
                    tensorMetadata.shape = new ArrayList<>();
                    for (int i = 0; i < m.size(); ++i) {
                        tensorMetadata.shape.add((Integer) m.get(Integer.toString(i)));
                    }
                }
                {
                    Map m = (Map) map.get("data_offsets");
                    Integer begin = (Integer) m.get("0"), end = (Integer) m.get("1");
                    tensorMetadata.dataOffsets = new AbstractMap.SimpleEntry<>(begin, end);
                    bufferSize = Math.max(bufferSize, end);
                }
            }
        }

        {
            byte[] buffer = new byte[bufferSize];
            int read = dataInputStream.read(buffer);
            assert bufferSize == read;
            safetensors.byteBuffer = ByteBuffer.wrap(buffer);
        }

        dataInputStream.close();
        return safetensors;
    }

    List<Integer> getShape(String tensorName) {
        assert header.containsKey(tensorName);
        return header.get(tensorName).shape;
    }

    FloatBuffer getFP32(String tensorName) {
        assert header.containsKey(tensorName);
        TensorMetadata tensorMetadata = header.get(tensorName);
        assert tensorMetadata.dtype.equals("FP32");
        Integer begin = tensorMetadata.dataOffsets.getKey(), end = tensorMetadata.dataOffsets.getValue();
        return ByteBuffer.wrap(byteBuffer.array(), begin, end - begin).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
    }
}
