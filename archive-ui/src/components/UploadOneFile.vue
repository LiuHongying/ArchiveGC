<template>
  <div>
    <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" 
    :append-to-body="true">
      <el-form size="mini" :label-width="formLabelWidth">
        <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
          <el-upload
            :limit="100"
            :file-list="fileList"
            action
            :on-change="handleChange"
            :auto-upload="false"
            :multiple="false"
          >
            <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
          </el-upload>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button
          type="primary"
          @click="uploadData(uploadID)"
        >{{$t('application.start')+$t('application.Import')}}</el-button>
      </div>
    </el-dialog>
    <el-button type="primary" @click="beforeMount(docId)">
      <slot>上传文件</slot>
    </el-button>
  </div>
</template>
<script>
export default {
  name: "UploadFile",
  components: {},
  props: {
      docId:{type:String,default:""},
      url:{type:String,default:""}
  },
  data() {
    return {
        importdialogVisible:false,
        uploadID: "",
        fileList:[]
    };
  },
  methods: {
    handleChange(file, fileList) {
      this.fileList = fileList;
    },
    //挂载
    beforeMount(id) {
      let _self = this;
      _self.fileList = [];
      if (id == undefined) {
        //  _self.$message("请选择一条数据！");
        _self.$message({
          showClose: true,
          message: "请选择一条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.uploadID = id;
      _self.importdialogVisible = true;
    },
    getFormData(selId) {
      let _self = this;
      let formdata = new FormData();
      var data = {};
      data["ID"] = selId;
      formdata.append("metaData", JSON.stringify(data));
      _self.fileList.forEach(function(file) {
        //console.log(file.name);
        formdata.append("uploadFile", file.raw, file.name);
      });
      return formdata;
    },

    //上传文件
    uploadData(selId) {
      let _self = this;
      let formdata = _self.getFormData(selId);
      //console.log("UploadData getData");
      //console.log(formdata);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          datatype: "json",
          method: "post",
          data: formdata,
          url: _self.url
        })
        .then(function(response) {
          _self.importdialogVisible = false;
          // _self.refreshData();
          _self.$emit("refresh",selId);
          // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
          _self.$message({
            showClose: true,
            message:
              _self.$t("application.Import") + _self.$t("message.success"),
            duration: 2000,
            type: "success"
          });
        })
        .catch(function(error) {
          console.log(error);
        });
    }
  }
};
</script>
<style scoped>
</style>