<template>
  <div>
    <div :style="{position:'relative',height: asideHeight+'px'}">
      <split-pane split="vertical" @resize="resize" min-percent="10" :default-percent="15">
        <template slot="paneL">
          <el-container :style="{height:asideHeight+'px',width:asideWidt,overflow:'auto'}">
            <el-tree
              style="width:100%"
              :props="defaultProps"
              :data="dataList"
              node-key="id"
              :render-content="renderContent"
              :default-expand-all="isExpand"
              highlight-current
              @node-click="handleNodeClick"
            ></el-tree>
          </el-container>
        </template>
        <template slot="paneR">
          <el-row>
            <el-col :span="4" class="topbar-input">
              <el-input
                v-model="inputkey"
                :placeholder="$t('message.pleaseInput')+$t('application.keyword')"
                @change="searchItem"
                prefix-icon="el-icon-search"
              ></el-input>
            </el-col>
            <el-col :span="4" style="padding:15px;">
              <el-radio style="margin-right:5px;" v-model="radio" label="案卷" @change="changeRadio">案卷</el-radio>
              <el-radio style="margin-left:5px;" v-model="radio" label="文件" @change="changeRadio">文件</el-radio>
            </el-col>
             <el-col :span="16" style="padding-top:8px;">
               <el-button type="warning" @click="onDeleleItem()">删除</el-button>
               <el-button type="primary" @click="reset()">恢复</el-button>
            </el-col>
          </el-row>
          <el-row>
            <DataGrid
              ref="mainDataGrid"
              key="main"
              dataUrl="/dc/getInnerFolderDocuments"
              :isInitData="false"
              v-bind:tableHeight="rightTableHeight"
              :isshowOption="true"
              :isshowSelection="true"
              :isShowChangeList="false"
              showOptions="查看内容"
              :optionWidth = "2"
              :isEditProperty="false"
              :condition="mainParam.condition"
              :folderId="mainParam.folderId"
              gridViewName="RecyclebinGrid"
              @rowclick="beforeShowInnerFile"
              @selectchange="selectChange"
            ></DataGrid>
            <div>
              <span style="float:left;text-align:left;padding:10px;">卷内文件列表</span>
              <DataGrid
                ref="leftDataGrid"
                key="left"
                @rowclick="selectOneFile"
                dataUrl="/dc/getDocuByRelationParentId"
                gridViewName='ArrangeInnerGrid'
                condition="and a.NAME='irel_children' and b.IS_HIDDEN=1"
                :parentId="parentId"
                v-bind:tableHeight="rightTableHeight"
                :isshowOption="true"
                :isshowSelection="true"
                :isShowChangeList="false"
                @selectchange="selectInnerChange"
              ></DataGrid>
            </div>
          </el-row>
        </template>
      </split-pane>
    </div>
  </div>
</template>

<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import DataGridleft from "@/components/DataGrid";
export default {
  name: "FolderClassification",
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
  },
  data() {
    return {
      isExpand: false,
      rightTableHeight: (window.innerHeight - 232) / 2,
      asideHeight: window.innerHeight - 85,
      asideWidth: "100%",
      currentLanguage: this.getLang(),
      dataList: [],
      radio: "案卷",
      innerSelectedOne: [],
      currentFolder: [],
      inputkey: "",
      loading: false,
      selectedItems: [],
      selectedInnerItems: [],
      selectRow: [],
      selectedFileId: "",
      tableHeight: window.innerHeight - 428,
      defaultProps: {
        children: "children",
        label: "name"
      },
      mainParam:{
        folderId:"",
        condition:""
      },
      parentId:"",
      folderId:"",
      archiveStatus:"",
    };
  },
  
  created() {
    this.loadTree();
  },
  methods: {
    loadTree(){
      let _self = this;
      var psize = localStorage.getItem("docPageSize");
      if (psize) {
        _self.pageSize = parseInt(psize);
      }
      _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
      _self.loading = true;
      let m=new Map();
      m.set("folderConfig","ArchiveCollatedID");
      m.set("condition"," and IS_HIDDEN=1 and IS_CHILD=0");
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/folder/getFolderByConfigeGC"
        })
        .then(function(response) {
          _self.dataList = response.data.data;

          _self.handleNodeClick(_self.dataList[0]);
          _self.isExpand = true;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    resize() {
      //console.log('resize')
      this.asideWidth = "100%";
    },
    selectOneFile(row) {
      let _self = this;
      if (row != null) {
        _self.innerSelectedOne = row;
        _self.selectedFileId = row.ID;
      }
    },
    changeRadio(val) {
      let _self = this;
      _self.$refs.leftDataGrid.cleanData();
      _self.loadGridData(_self.currentFolder);
      
    },
    beforeShowInnerFile(row) {
      this.innerSelectedOne = [];
      this.showInnerFile(row);
    },
    showInnerFile(row) {
      let _self = this;
      if (row != null) {
        _self.selectRow = row;
      }
      _self.parentId=row.ID;
      _self.$nextTick(()=>{
        _self.$refs.leftDataGrid.loadGridData();
      });
    },
    renderContent: function(h, { node, data, store }) {
      if (data.extended) {
        return (
          <span>
            <i class="el-icon-folder-opened"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      } else {
        return (
          <span>
            <i class="el-icon-folder"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      }
    },
    // 表格行选择
    selectChange(val) {
      // console.log(JSON.stringify(val));
      this.selectedItems = val;
    },
    selectInnerChange(val) {
      this.selectedInnerItems = val;
    },
    // 加载表格数据
    loadGridData(indata) {
      let _self = this;

      var key = _self.inputkey;
      
      if (key != "") {
        key = " (coding like '%" + key + "%' or title like '%" + key + "%') ";
        if (_self.radio == "案卷") {
          key= key + " and C_ITEM_TYPE='案卷' and IS_HIDDEN=1 ";
        } else {
          key= key + " and C_ITEM_TYPE='文件' and IS_CHILD=0 and IS_HIDDEN=1 ";
        }
        
      } else {
        if (_self.radio == "案卷") {
          key=key+ " C_ITEM_TYPE='案卷' and IS_HIDDEN=1 ";
        } else {
          key=key+" C_ITEM_TYPE='文件' and IS_CHILD=0 and IS_HIDDEN=1";
        }
       
      }
      if(_self.archiveStatus!=''){
          key=key+" and status='"+_self.archiveStatus+"'";
        }
      _self.mainParam.condition=key;
      _self.mainParam.folderId=indata.id
      _self.$nextTick(()=>{
        _self.$refs.mainDataGrid.loadGridData();
      });
      
      
    },
    // 文件夹节点点击事件
    handleNodeClick(indata) {
      let _self = this;
      _self.selectRow = [];
      _self.selectedFileId = "";

      _self.currentFolder = indata;
      //console.log(JSON.stringify(indata));
      // 没有加载，逐级加载
      if (indata.extended == false) {
        _self.loading = true;
        _self
          .axios({
            headers: {
              "Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: indata.id,
            url: "/folder/getArchiveFolderByConfige" //"/admin/getFolder"
          })
          .then(function(response) {
            indata.children = response.data.data;
            //console.log(JSON.stringify(indata));
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function(error) {
            console.log(error);
            _self.loading = false;
          });
      }
      // _self.loadGridInfo(indata);
      _self.loadGridData(indata);
    },
    //查询文档
    searchItem() {
      this.loadGridData(this.currentFolder);
    },
    onDeleleItem() {
      let _self = this;
      this.$confirm(
        _self.$t("message.deleteInfo"),
        _self.$t("application.info"),
        {
          confirmButtonText: _self.$t("application.ok"),
          cancelButtonText: _self.$t("application.cancel"),
          type: "warning",
        }
      )
        .then(() => {
          _self.deleleItem();
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // });
        });
    },
    deleleItem() {
      let _self = this;
      var m = [];
      let tab = _self.selectedItems;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      axios
        .post("/dc/delDocument", JSON.stringify(m))
        .then(function (response) {
          _self.loadGridData(_self.currentFolder);
          _self.$message(_self.$t("message.deleteSuccess"));
        })
        .catch(function (error) {
          _self.$message(_self.$t("message.deleteFailured"));
          console.log(error);
        });
    },
    reset(){
      //恢复
      let _self=this;
      if(_self.selectedItems==''||_self.selectedItems==undefined){
        _self.$message({
            showClose: true,
            message:"请选择一个文档",
            duration: 2000,
            type: "warning",
          });
        return
      }
      var m = [];
      let tab = _self.selectedItems;
      var i;
      for (i in tab) {
        m.push(tab[i]["ID"]);
      }
      axios.post("/dc/recyclebin/resetDC",JSON.stringify(m),{
        headers: {
            "Content-Type": "application/json;charset=UTF-8"
        }
      })
      .then(function (response) {
        let code = response.data.code;
        if (code == 1) {
          _self.$message({
            showClose: true,
            message:"恢复成功",
            duration: 2000,
            type: "success",
          });
          _self.loadTree();
          _self.$nextTick(()=>{
            _self.$refs.leftDataGrid.cleanData();
            _self.loadGridData(_self.currentFolder);
          });
         
          
          // 刷新
        } else {
          _self.$message({
            showClose: true,
            message:"恢复失败",
            duration: 2000,
            type: "warning",
          });
        }
      })
      .catch(function (error) {
        _self.$message("恢复失败");
        console.log(error);
      });
    },
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
body,
html {
  height: 100%;
  margin: 0px;
  padding: 0px;
  overflow: hidden;
}
.left,
.right {
  width: 47.5%;
}
.middle {
  width: 5%;
}
.left,
.middle,
.right {
  float: left;
  height: 100%;
}
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.el-select-dropdown.is-multiple .el-select-dropdown__item.selected::after {
  position: absolute;
  right: 2px;
  font-family: element-icons;
  content: "\E6DA";
  font-size: 12px;
  font-weight: 700;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>