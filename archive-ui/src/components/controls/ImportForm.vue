<template>
  <el-form label-width="120px" @submit.native.prevent>
    <el-row>
      <el-col :span="16" style="text-align:left">
        <el-button type="primary" plain icon="el-icon-upload2" @click="batchImport()" v-loading="loading">{{$t('application.start')+$t('application.Import')}}</el-button>
        &nbsp; &nbsp;
         <el-button plain type="primary" @click="cleanFiles()">{{$t('message.ClearFiles')}}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="20">
        <el-progress :text-inside="true" :stroke-width="24" :percentage="progressNum" status="success"></el-progress>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="10">
        <el-form-item :label="'Excel'+$t('message.file')" style="float: left;">
          <el-upload
            :limit="1"
            :file-list="fileList1"
            action
            :on-change="handleChange1"
            :auto-upload="false"
            :multiple="false"
          >
          &nbsp; &nbsp;
            <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
          </el-upload>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-input type="textarea" :rows="6" v-model="importMessage"></el-input>
      </el-col>
    </el-row>
  </el-form>
</template>

<script type="text/javascript">
export default {
  name: "ImportDocument",
  data() {
    return {
      loading: false,
      fileList1: [],
      importMessage: "",
      templateData:[],
      selectedTemplate:"",
      formLabelWidth: "120px",
      progressNum:0
    };
  },
  mounted() {
    this.progressNum=0;
  },
  methods: {
    handleChange1(file, fileList) {
      this.fileList1 = fileList;
    },
    batchImport() {
      let _self = this;
      if (_self.fileList1 == null || _self.fileList1.length == 0||_self.fileList1[0].raw==null) {
         _self.$message(_self.$t('application.PleaseSelect'));
        return;
      }
      _self.loading = true;
      let formdata = new FormData();
      formdata.append("excel", _self.fileList1[0].raw);
      let postUrl = "/FormImport/batchImport"
      axios.post(postUrl, formdata,{headers:{"Content-Type": "multipart/form-data"}})
        .then(function(response) {
          _self.importMessage = response.data.data;
          _self.loading = false;
          _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
          _self.cleanFiles();
          _self.$emit("onImported");
          
        })
        .catch(function(error) {
          _self.$message(_self.$t('application.importFailed'));
          _self.loading = false;
          console.log(error);
        });
    },
    cleanFiles(){
      this.fileList1 = [];
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1,
h2 {
  font-weight: normal;
}
/* ul {
  list-style-type: none;
  padding: 0;
 
} */
/* ul.el-upload-list{
   height: 200px;
  overflow: scroll;
} */
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.el-upload-list {
  
    margin: 0;
    padding: 0;
    list-style: none;
    max-height: 300px !important;
    overflow: scroll !important;
    
}

</style>

