package top.hungrywu.bean.kiwi;

import lombok.Builder;
import lombok.Data;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/27 1:09 下午
 * @Version 1.0
 **/
@Data
@Builder
public class UpdateKiwiPageRequestData {

    /**
     * id : 3604482
     * type : page
     * title : new page
     * space : {"key":"TST"}
     * body : {"storage":{"value":"<p>This is the updated text for the new page<\/p>","representation":"storage"}}
     * version : {"number":2}
     */

    private String id;
    private String type;
    private String title;
    private SpaceBean space;
    private BodyBean body;
    private VersionBean version;


    @Data
    @Builder
    public static class SpaceBean {
        /**
         * key : TST
         */

        private String key;
    }

    @Data
    @Builder
    public static class BodyBean {
        /**
         * storage : {"value":"<p>This is the updated text for the new page<\/p>","representation":"storage"}
         */

        private StorageBean storage;

        @Data
        @Builder
        public static class StorageBean {
            /**
             * value : <p>This is the updated text for the new page</p>
             * representation : storage
             */

            private String value;
            private String representation;

        }
    }

    @Data
    @Builder
    public static class VersionBean {
        /**
         * number : 2
         */

        private int number;
    }
}
