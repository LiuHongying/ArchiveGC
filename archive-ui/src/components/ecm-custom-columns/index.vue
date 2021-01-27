<template>
  <el-container>
    <el-header>
      <el-row>
        <!-- Matthew changes on 2021年1月25日17:07:23 -->
          <el-form size="small" label-width="100" inline>
            <el-form-item
            :label="$t('application.fileType')"
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
          >
            <el-select
              name="selectName"
              v-model="selectedTypeName"
              :placeholder="$t('application.selectFileType')"
              @change="getCustomization(selectedTypeName)"
            >
              <template v-for="(name,nameIndex) in typeNames" >
                <el-option :label="name" :value="name" :key="'CT_'+nameIndex"></el-option>
              </template>
            </el-select>
          </el-form-item>
             <!-- end -->
          <el-form-item label="配置列表">
              <el-select v-model="selectedName" @change="refreshConfig(selectedName)">
                <!-- Matthew changes on 2021年1月26日15:08:48 -->
                <!-- <el-option v-for="item in customNames" :key="item.name" :label="item.description" :value="item.name" @click.native="onSelectChange(item)"></el-option> -->
                <el-option v-for="item in customNames" :key="item.id" :label="item.name" :value="item.id" @click.native="refreshConfig(item)"></el-option>
                <!-- end -->
              </el-select>
            </el-form-item>
            <el-form-item>
              <!-- <el-button type="primary" @click="useConfig">{{$t('application.useConfig')}}</el-button> -->
              <el-button type="success" @click="updateConfig">{{$t('application.updateConfig')}}</el-button>
              <el-button type="danger" @click="deleteListConfig">{{$t('application.deleteListConfig')}}</el-button>
            </el-form-item>
          </el-form>
        </el-row>
    </el-header>
    <el-main>
      <el-row>
        <el-col :span="8">
          <el-input v-model="searchS" :placeholder="$t('application.InputFilterKeyWord')"></el-input>
          <el-table ref="sourceTable" :data="tables.source.data.filter(data => !searchS || data.label.toLowerCase().includes(searchS.toLowerCase()) || data.attrName.toLowerCase().includes(searchS.toLowerCase()))" v-bind="tables.source.attrs" @selection-change="onSelectTableDataSource">
            <el-table-column type="selection" width="45"></el-table-column>
            <el-table-column :label="$t('field.indexNumber')" type="index" width="60"></el-table-column>
            <el-table-column :label="$t('application.property')" prop="label"></el-table-column>
            <el-table-column :label="$t('application.operation')" width="75" fixed="right">
              <template slot-scope="scope">
                <el-button :plain="true" type="primary" size="small" icon="edit" @click="addItem(scope.row)">添加</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="1" class="center_buttons">
          <el-row>
            <el-col :span="24">&nbsp;</el-col>
            <el-col :span="24">&nbsp;</el-col>
            <el-col :span="24">
              <el-button @click="addItemToTarget" size="small">{{$t('application.Add')}}</el-button>
            </el-col>
            <el-col :span="24">
              <el-button @click="removeTagetRow" size="small">{{$t('application.remove')}}</el-button>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="15">
          <el-input v-model="searchT" :placeholder="$t('application.InputFilterKeyWord')"></el-input>
          <el-table ref="targetTable" :data="tables.target.data.filter(data => !searchT || data.label.toLowerCase().includes(searchT.toLowerCase()) || data.attrName.toLowerCase().includes(searchT.toLowerCase()))" v-bind="tables.target.attrs"  @selection-change="onSelectTableDataTarget">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column :label="$t('field.indexNumber')" type="index" width="60"></el-table-column>
            <el-table-column :label="$t('application.property')" prop="label"></el-table-column>
            <el-table-column label="宽度" width="100">
              <template slot-scope="scope">
                <el-input v-model="scope.row.width"></el-input>
              </template>
            </el-table-column>
            <el-table-column :label="$t('application.showType')" width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.visibleType">
                  <el-option label="显示" value="1"></el-option>
                  <el-option label="可选" value="2"></el-option>
                  <el-option label="隐藏" value="3"></el-option>
                </el-select>
              </template>
          </el-table-column>
          <el-table-column label="可排序" width="100">
            <template slot-scope="scope">
              <el-select v-model="scope.row.allowOrderby">
                <el-option label="是" :value="true"></el-option>
                <el-option label="否" :value="false"></el-option>
              </el-select>
            </template>
          </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                
                <el-button size="mini" @click="moveUp(scope.$index,scope.row)">上移</el-button>
                <el-button size="mini" @click="moveDown(scope.$index,scope.row)">下移</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-main>
    <el-footer style="text-align: right;">
      <!-- <el-button @click="saveCustomColumn">{{$t('application.save')}}</el-button> -->
      <el-button type="primary" @click="saveListConfig">{{$t('application.saveNewListConfig')}}</el-button>
      <el-button @click="cancelCustomColumn">{{$t('application.cancel')}}</el-button>
    </el-footer>
  </el-container>
</template>

<script>
export default {
  name:"EcmCustomColumns",
  props:{
    typeName:{
      type:String,default:""
    },
    gridViewName:{
      type:String,default:""
    },
    sysColumnInfo:{
      type:Array,
      default:function(){
        return []
      }
    }
  },
  data(){
    return{
      //Matthew changes on 2021年1月25日17:09:06
      typeNames: [],
      selectedTypeName: "",
      classicNames: [],
      selectedClassic:"",
      configId:"",
      // end
      customNames:[],
      selectedName:"",
      currentLanguage:"zh-cn",
      searchS:"",
      searchT:"",
      tables:{
        source:{
          attrs:{
            border:true,
            height:300
          },
          data:[],
          selections:[]
        },
        target:{
          attrs:{
            border:true,
            height:300
          },
          data:[],
          selections:[]
        }
      }
    }
  },
  mounted(){
    this.getClassicNames("ClassicNames");
    //this.loadCustomName()
    this.loadSysColumnInfo()
    this.tables.source.data = this.sysColumnInfo
  },
  watch: {
    '$store.state.app.language':function(nv,ov){
        this.currentLanguage=nv;        
      }
  },
  methods:{
    //Matthew changes on 2021年1月25日17:09:23
    useConfig(){
      let _self = this;
      _self.$emit("loadUserListConfig", _self.tables.target.data);
      _self.cancelCustomColumn();
    },
    refreshConfig(item){
      let _self = this;
      _self.configId = item.id
      let url = "/archive/getConfigById";
      let m = new Map();
      m.set("id",item.id);
      axios.post(url,JSON.stringify(m)).then(function(response){
            if(response.data.code==1) {
              _self.tables.target.data = JSON.parse(response.data.data)
            }
          })
    },
    refreshConfigList(typeName){
      let _self = this;
      let url = "/archive/getConfigList";
      let m = new Map();
      m.set("typeName",typeName);
      axios.post(url,JSON.stringify(m)).then(function(response){
            if(response.data.code==1) {
              _self.customNames = response.data.data
            }
          })
    },
    saveListConfig(){
      let _self = this;
      if(_self.tables.target.data.length<1){
        _self.$message({ message: '没有选择项，无法保存', type: 'warning' })
        return;
      }
      var screenConfigArr = [];
      var attrsStr = "";
      for(var i =0;i<_self.tables.target.data.length;i++){
        let currentData = _self.tables.target.data[i]
        attrsStr=attrsStr+currentData.attrName+","
        let dataMap = {
          "id":currentData.id,
          "attrName":currentData.attrName,
          "orderIndex":i+1,
          "label":currentData.label,
          "width":currentData.width,
          "visibleType":currentData.visibleType,
          "allowOrderby":currentData.allowOrderby
        }
        screenConfigArr.push(dataMap)
      }
      this.$prompt('请输入配置名称', '新建配置', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }).then(({ value }) => {
          //配置名称
          _self.selectedName = value
          //文档类型
          var typeName = _self.selectedTypeName
          //配置信息
          var configContent = JSON.stringify(screenConfigArr)
          let m = new Map();
          m.set('configName',_self.selectedName);
          m.set('typeName',typeName);
          m.set('configContent',configContent);
          m.set('attrsStr',attrsStr)
          let url = "/archive/customListConfig"
          axios.post(url,JSON.stringify(m)).then(function(response){
            if(response.data.code==1) {
              _self.$message({showClose: true,message:_self.$t('message.saveSuccess'),duration: 2000,type: "Success"});
              _self.$emit("loadMainListConfig");
              _self.refreshConfigList(_self.selectedTypeName)
            }
          })
        }).catch();
    },
    updateConfig(){
      let _self = this;
      if(_self.tables.target.data.length<1||_self.configId == ""){
        _self.$message({ message: '没有选择项，无法更新', type: 'warning' })
        return;
      }
      var screenConfigArr = [];
      for(var i =0;i<_self.tables.target.data.length;i++){
        let currentData = _self.tables.target.data[i]
        let dataMap = {
          "id":currentData.id,
          "attrName":currentData.attrName,
          "orderIndex":i+1,
          "label":currentData.label,
          "width":currentData.width,
          "visibleType":currentData.visibleType,
          "allowOrderby":currentData.allowOrderby
        }
        screenConfigArr.push(dataMap)
      }
      var configContent = JSON.stringify(screenConfigArr)
      let mp = new Map();
      let url = "/archive/deleteOrUpdateConfig";
      mp.set("id",_self.configId);
      mp.set("configContent",configContent);
      mp.set("aciton","update");
      axios.post(url,JSON.stringify(mp)).then(function(response){
            if(response.data.code==1) {
              _self.$message({showClose: true,message:'更新成功',duration: 2000,type: "Success"});
              _self.refreshConfigList(_self.selectedTypeName)
            }
          })
    },
    deleteListConfig(){
      let mp = new Map();
      let _self = this;
      let url = "/archive/deleteOrUpdateConfig";
      mp.set("id",_self.configId);
      mp.set("aciton","delete");
      axios.post(url,JSON.stringify(mp)).then(function(response){
            if(response.data.code==1) {
              _self.$message({showClose: true,message:'删除成功',duration: 2000,type: "Success"});
              _self.selectedName = "";
              _self.configId = "";
              _self.tables.target.data = [];
              _self.refreshConfigList(_self.selectedTypeName)
            }
          })
    },
    getCustomization(keyName){
      this.selectedName = "";
      this.configId = "";
      this.refreshConfigList(keyName)
      this.loadCustomColumnInfo(keyName)
      this.tables.target.data = []
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
    loadCustomColumnInfo(keyName){
      let _self = this
      let url = "/archive/getFormItems/"+keyName+"/"+this.currentLanguage
      axios.post(url).then(function(response){
        _self.tables.source.data = response.data.data
      }).catch(function(error){
        console.log(error)
      })
    },
    // end
    loadSysColumnInfo(){
      let _self = this
      let url = "/admin/getFormItems/"+this.gridViewName+"/"+this.currentLanguage
      axios.post(url).then(function(response){
        _self.tables.source.data = response.data.data
      }).catch(function(error){
        console.log(error)
      })
    },
    onSelectChange(item){
      let id = item.id
      let name = item.name
      let desc = item.description
     this.loadCustomInfo(id,name,desc)
    },
    loadCustomInfo(id,name,desc){
      let _self=this;
      var m = new Map();
      m.set('gridId',id);
      m.set("lang", _self.currentLanguage);
      let url = "/dc/getOneEcmCustomGridViewInfo" 
      axios.post(url,JSON.stringify(m)).then(function(response) {
        if(response.data.code==1){
          _self.tables.target.data = response.data.data
          _self.selectedName = name
        }
      }).catch(function(error){
        console.log(error);
      })
    },
    saveCustomColumn(){
      let _self=this;
      if(_self.selectedName==''){
        _self.$message({ message: '名称为空！', type: 'warning' })
        return;
      }
      let uname = this.currentUser().userName
      let gvname = this.selectedName.replace(uname+"_","")
      let datalist = this.tables.target.data
      let postArray = new Array()
      for (let index = 1; index <= datalist.length; index++) {
        let item = datalist[index-1]
        item.orderIndex=index
        let removeAttrs = ["enableChange","defaultValue","classification","maxCount","required",
        "searchable","isHide","controlType","valueList","isRepeat",
        "readOnly","parentId","minCount","widthType","queryName",
        "id","validatePolicy"]
        removeAttrs.forEach(function(attr){
          delete item[attr]
        })
        postArray.push(item)
      }
      let postData = new Map();
      postData.set("gridName",this.gridViewName)
      postData.set("NAME",gvname)
      postData.set('items',postArray);
      let url = "/admin/createOrUpdateGridView"
      axios.post(url,JSON.stringify(postData)).then(function(response){
        if(response.data.code==1){
          _self.$message({showClose: true,message:_self.$t('message.saveSuccess'), duration: 2000,type: "Success"});
          _self.clearData()
          _self.$emit("onClose")
        }
      }).catch(function(error){
        console.log(error)
      })
    },
    
    cancelCustomColumn(){
      this.clearData()
      this.$emit("onCancel")
    },
    clearData(){
      this.$refs.targetTable.clearSelection()
      this.selectedName=""
      this.tables.target.data=[]
      this.tables.source.selections=[]
      this.tables.target.selections=[]  
    },
    loadCustomName(){
      let _self = this
      let url = "/admin/getAllGridViewsOfCurrentUser"
      let params = {
        "gridName":this.gridViewName
      }
      axios.post(url,JSON.stringify(params)).then(function(response){
        if(response.data.code==1){
          _self.customNames=response.data.data
        }
      }).catch(function(error){
        console.log(error)
      })
    },
    onSelectTableDataSource(val){
      this.tables.source.selections = val
    },
    onSelectTableDataTarget(val){
      this.tables.target.selections = val
    },
    addItem(row){
      this.tables.source.selections.push(row)
      this.addItemToTarget()
      this.tables.source.selections=[]
    },
    addItemToTarget(){
      let _self = this
      let selections = this.tables.source.selections
      let targetList = this.tables.target.data
      selections.forEach(function(item){
          let isExist = false
          targetList.forEach(function(sub){
            if(item.attrName == sub.attrName){
              isExist = true
            }
          })
          if(isExist==false){
            item.width="200"
            item.visibleType="1"
            item.allowOrderby=true
            _self.tables.target.data.push(item)
          }
      })
    },
    removeTagetRow(){
      let datalist = this.tables.target.data
      let selection = this.tables.target.selections
      let itemlist = new Array()
      selection.forEach(function(item){
        itemlist.push(item.attrName)
      })
      for (let index = datalist.length-1 ; index >=0 ; index--) {
        const atname = datalist[index].attrName
        if(itemlist.includes(atname)){
          datalist.splice(index,1)
        }
      }
      this.tables.target.data = datalist
    },
    moveUp(index, row){
      if (index == 0) {
        this.$message({
            message: '处于顶端，不能继续上移',
            type: 'warning'
          });
      }else{
        let upData = this.tables.target.data[index - 1]
        this.tables.target.data.splice(index - 1, 1)
        this.tables.target.data.splice(index, 0, upData)

      }
    },
    moveDown(index, row){
      if (index === (this.tables.target.data.length - 1)) {
        this.$message.error('不能继续向下移动')
      } else {
        const downData = this.tables.target.data[index + 1]
        this.tables.target.data.splice(index + 1, 1)
        this.tables.target.data.splice(index, 0, downData)
      }
    },
    createGridView(){
      let _self = this
      this.$prompt('请输入列名称', '新建列名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }).then(({ value }) => {
          _self.selectedName = value
          let m = new Map();
          m.set('gridName',_self.gridViewName);
          m.set('NAME',_self.selectedName);
          let url = "/admin/createOrUpdateGridView"
          axios.post(url,JSON.stringify(m)).then(function(response){
            if(response.data.code==1) {
              _self.$message({showClose: true,message:_self.$t('message.saveSuccess'),duration: 2000,type: "Success"});
              _self.loadCustomName();
            }
          })
        }).catch();
    },
    deleteGridView(){
      let _self = this
      let m = new Map();
      m.set('gridName',this.gridViewName);
      m.set('NAME',this.selectedName);
      m.set("lang", this.currentLanguage);
      let url = "/admin/deleteCustomGridView"
      axios.post(url,JSON.stringify(m)).then(function(response){
        if(response.data.code==1){
          _self.$message({showClose: true,message: _self.$t('message.deleteSuccess'),
duration: 2000,type: "Success"});
          _self.selectedName=""
          _self.loadCustomName()
        }
      }).catch(function(error){})
    }
  }
}
</script>

<style scoped>
.center_buttons .el-row .el-col{
  text-align: center;
  display: inline-block;
  vertical-align: middle;
}
</style>