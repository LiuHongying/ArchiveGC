<template>
  <div>
    <div>
      <el-form label-width="120px" v-loading="loading" @submit.native.prevent>
            <el-row>
              <div style="float: left;text-align:left;">
                &nbsp; &nbsp; 根据文件名挂载到文件或卷内文件，单个文件挂载不约束文件名。
                <br/>
                多个文件挂载命名规则：编码(版本)，如：1188XGASBS01(A).pdf,1188XGASBS01.pdf(无版本)。
                <br />
              </div>
            </el-row>
            <el-row>
              <el-col :span="8">
                <el-form-item :label="'Excel'+$t('message.ElectronicFiles')" style="float: left;">
                  <el-upload
                    :limit="100"
                    :file-list="fileList"
                    action
                    :on-change="handleChange"
                    :auto-upload="false"
                    :multiple="true"
                  >
                    <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :span="16" style="float: left;text-align:left;padding:5px;">
                <el-button type="primary" plain icon="el-icon-upload2" @click="batchMount()">开始挂载</el-button>
                <el-button plain type="primary" @click="cleanFiles()">{{$t('message.ClearFiles')}}</el-button>
              </el-col>
            </el-row>
        <el-row>
          <el-col>
            <el-input type="textarea" :rows="6" v-model="importMessage"></el-input>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>
<script type="text/javascript">
export default {
  name: "BatchFileMount",
  permit: 1,
  data() {
    return {
      activeName: "first",
      fileList:[],
      importMessage:"",
      loading:false
    };
  },
   props: {
    archiveObjects:{type:Array,default:() => []}
  },
  methods: {
    handleChange(file, fileList) {
      this.fileList = fileList;
    },
    
    cleanFiles(){
      this.fileList = [];
    },
    batchMount() {
      let _self = this;
      var m = [];
      let tab = _self.archiveObjects;  
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      let formdata = new FormData();
      formdata.append("ids",m);
      _self.fileList.forEach(function(file) {
        formdata.append("files", file.raw, file.name);
      });
      _self.loading = true;
      axios
        .post("/dc/batchMountFile", formdata, {
          "Content-Type": "multipart/form-data"
        })
        .then(function(response) {
          _self.importMessage = response.data.data;
          _self.loading = false;
          _self.$message("挂载成功!");
          _self.cleanFiles();
          _self.$emit("afterMountFile");
        })
        .catch(function(error) {
          _self.$message("挂载失败!");
          console.log(error);
        });
    }
  }
};
</script>
<style scoped>
</style>