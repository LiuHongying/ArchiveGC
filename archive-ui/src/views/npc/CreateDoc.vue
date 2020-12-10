<template>
  <DataLayout>
    <template v-slot:header></template>
    <template v-slot:main>
      <el-row>
        <el-col :span="3" class="el-form-item__label">模板</el-col>
        <el-col :span="3" class="el-form-item__content">模板1</el-col>
        <el-col :span="6" class="el-form-item__content">
          <el-button type="primary">预览</el-button>
          <el-button type="primary">下载</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <ShowProperty
            ref="ShowProperty"
            @onSaved="onSaved"
            @onSaveSuccess="onPropertiesSaveSuccess"
            width="95%"
            typeName="会议纪要"
            :showUploadFile="false"
            v-bind:itemId="selectedItemId"
          ></ShowProperty>
        </el-col>
      </el-row>
      <el-row>
        <el-collapse v-model="uploadFileModel">
          <el-collapse-item
            title="上传文件"
            name="上传文件"
            id="uploadFile"
            key="cindex"
          >
          <el-row style="margin:10px">
            <el-col :span="3">上传主件：</el-col>
            <el-col :span="20">
              <el-upload
                :limit="1"
                :file-list="fileList"
                action
                :on-change="handleChange"
                :auto-upload="false"
                :multiple="false"
              >
                <el-button
                  slot="trigger"
                  size="small"
                  type="primary"
                >{{$t('application.selectFile')}}</el-button>
              </el-upload>
            </el-col>
          </el-row>
          <el-row style="margin:10px">
              <el-col :span="3">上传附件：</el-col>
                <el-col :span="20">
                    <el-upload
                        :limit="0"
                        :file-list="fileAttachList"
                        action
                        :on-change="handleChangeAttach"
                        :auto-upload="false"
                        :multiple="true"
                    >
                        <el-button
                        slot="trigger"
                        size="small"
                        type="primary"
                        >{{$t('application.selectFile')}}</el-button>
                    </el-upload>
                </el-col>
          </el-row>
          </el-collapse-item>
          <el-collapse-item
            title="启动流程"
            name="startupWorkflow"
            id="startupWorkflow"
            key="cindexstartupWorkflow"
          >
          </el-collapse-item>
        </el-collapse>
      </el-row>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import ShowProperty from "@/components/ShowProperty";
export default {
  name: "CreateDoc",
  data() {
    return {
      file: [],
      fileList: [],
      uploadFileModel:"",
      fileAttachList:[]
    };
  },
  mounted() {},
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty
  },
  methods: {
    handleChange(file, fileList) {
      this.file = file;
      //console.log(file);
      // console.log(fileList);
    },
    handleChangeAttach(file, fileList) {
        debugger
      this.fileAttachList = fileList;
      //console.log(file);
      // console.log(fileList);
    }
  }
};
</script>
<style scoped>
</style>
