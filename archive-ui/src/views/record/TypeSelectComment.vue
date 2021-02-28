<template>
  <div >
    <el-dialog :visible.sync="typeSelectVisible" width="400px">
      <div style="width: 80%">
        <el-form>
          <el-form-item
            :label="$t('application.fileType')"
            :rules="[{required:true,message:'必填',trigger:'blur'}]"
          >
            <el-select
              name="selectName"
              v-model="selectedClassic"
              :placeholder="$t('application.selectFileType')"
              @change="getTypeNameByClassic(selectedClassic)"
            >
              <template v-for="(name,nameIndex) in classicNames">
                <el-option :label="name" :value="name" :key="'PT_'+nameIndex"></el-option>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item
            :label="$t('application.fileType')"
            :rules="[{required:true,message:'必填',trigger:'blur'}]"
          >
            <el-select
              name="selectName"
              v-model="selectedTypeName"
              :placeholder="$t('application.selectFileType')"
            >
              <template v-for="(name,nameIndex) in typeNames" >
                <el-option :label="name" :value="name" :key="'CT_'+nameIndex"></el-option>
              </template>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button
            @click="typeSelectVisible=false;$emit('afterSelecteType',selectedTypeName)"
          >{{$t('application.ok')}}</el-button>
        </div>
      </div>
    </el-dialog>
    <el-button
      type="primary"
      plain
      size="small"
      @click="showdialog"
    >
      <slot>{{$t('application.newDocument')}}</slot>
    </el-button>
  </div>
</template>
<script>
export default {
  name: "TypeSelectComment",
  components: {},

  data() {
    return {
      typeSelectVisible: false,
      selectedTypeName: "",
      typeNames: [],
      classicNames: [],
      selectedClassic: ""
    };
  },
  props: {
    currentFolder:{type: Object, default: null}
  },
  mounted() {
    this.getClassicNames("ClassicNames");
  },
  methods: {
    showdialog(){
      this.typeSelectVisible=true;
      if(this.currentFolder!=null && this.classicNames != null && this.classicNames.length>0){
        var i;
        for(i in this.classicNames){
          if(this.currentFolder.folderPath.indexOf(this.classicNames[i])>-1){
            this.selectedClassic = this.classicNames[i];
            this.getTypeNameByClassic(this.selectedClassic);
            return;
            //
          }
        }
      }
    },
    getTypeNameByClassic(keyName) {
      let _self = this;
      _self.selectedTypeName = "";
      _self.typeNames = [];
      axios
        .post("/dc/getEcmDefTypes", keyName)
        .then(function(response) {
          if (response.data.code == 1) {
            response.data.data.forEach(element => {
              _self.typeNames.push(element.name);
            });
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    getClassicNames(keyName) {
      let _self = this;
      let pm = new Map();
      pm.set("configName", keyName);
      // pm.set('parentId',"'"+p+"'");
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(pm),
          url: "/dc/getObjectsByConfigClauseNoPage"
        })
        .then(function(response) {
          var i;
          let temp = response.data.data;
          for (i = 0; i < temp.length; i++) {
            _self.classicNames.push(temp[i].NAME);
          }
          console.log(_self.contractors);
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