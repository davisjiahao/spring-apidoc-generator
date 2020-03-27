package top.hungrywu.bean.kiwi;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description TODO
 * @Author daviswujiahao
 * @Date 2020/3/25 5:51 下午
 * @Version 1.0
 **/

@Builder
@Data
public class NewWikiPageRequestData {

    /**
     * type : page
     * ancestors : [{"id":339095512}]
     * title : new page234
     * space : {"key":"~daviswujiahao"}
     * body : {"storage":{"value":"||col1||col2||","representation":"storage"}}
     */

    private String type;
    private String title;
    private SpaceBean space;
    private BodyBean body;
    private List<AncestorsBean> ancestors;

    @Builder
    @Data
    public static class SpaceBean {
        /**
         * key : ~daviswujiahao
         */

        private String key;

    }
    @Builder
    @Data
    public static class BodyBean {
        /**
         * storage : {"value":"||col1||col2||","representation":"storage"}
         */

        private StorageBean storage;

        @Builder
        @Data
        public static class StorageBean {
            /**
             * value : ||col1||col2||
             * representation : storage
             */

            private String value;
            private String representation;

        }
    }

    @Builder
    @Data
    public static class AncestorsBean {
        /**
         * id : 339095512
         */

        private String id;
    }
}
