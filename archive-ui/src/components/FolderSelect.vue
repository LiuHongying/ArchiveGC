<template>
    <el-container>
        <el-main>
            <el-row>
                <el-col :span="4" :style="{height: treeHeight + 'px',overflow: 'auto'}">
                    <el-tree 
                    :data="folderList"
                    :props="defaultProps"
                    node-key="id"
                    style="width: 100%"
                    :render-content="renderContent"
                    default-expand-all
                    highlight-current
                    @node-click="handleNodeClick"
                    ></el-tree>
                </el-col>
                <el-col :span="20">
                    <el-input 
                    style="width:200px"
                    v-model="inputkey"
                    :placeholder="$t('message.pleaseInput')+$t('application.keyword')"
                    @keyup.enter.native="searchItem"
                    prefix-icon="el-icon-search"
                    ></el-input>
                    <DataGrid
                    ref="mainDataGrid"
                    key="mainDataGrid"
                    dataUrl="/dc/getInnerFolderDocuments"
                    :itemDataList= itemDataList
                    :loading="false"
                    :isInitData="false"
                    :isshowOption="true" 
                    :isshowSelection ="true"
                    :tableHeight= treeHeight
                    :gridViewName= gridViewName
                    :condition="mainParam.condition"
                    :folderId="mainParam.folderId"
                    :isshowCustom="false"
                    :isEditProperty="false"
                    :isShowMoreOption="true"
                    showOptions="查看内容,查看属性"
                    :isShowChangeList="false"
                    :isShowPropertyButton="false"
                    @selectchange="docSelect"
                ></DataGrid>
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>

<script type="text/javascript">
import DataGrid from "@/components/DataGrid";

export default {
    
    components:{
        DataGrid:DataGrid
    },
    data(){
        return{
            itemDataList: [],
            finalDataList:{},
            inputkey:"",
            mainParam:{
            folderId:"",
            condition:"" 
            },
            folderList:[],
            treeHeight: window.innerHeight - 360,
            defaultProps: {
                children: "children",
                label: "name",
            },
            currentFolder: [],
            loading: false,
        }
    },
    props: {
        //父目录路径
        folderPath: {type: String, default: "/预归档库"},
        //gridview的名字
        gridViewName: {type: String, default: "ResearchBorrowGrid"}
    },
    watch:{},
    created(){
        let _self = this;
        _self.getFolders(_self.folderPath);
    },
    methods:{
        docSelect(val){
            let _self = this;
            _self.finalDataList = val
        },
        loadGridData(indata){
            let _self = this;
            _self.$nextTick(()=>{
                let key = _self.inputkey;
                if(key != ""){
                    key = " coding like '%" +
                            key +
                            "%' or title like '%" +
                            key +
                            "%' ";
                _self.mainParam.condition=key;
                }
                _self.mainParam.folderId=indata.id
                _self.$nextTick(()=>{
                    _self.$refs.mainDataGrid.loadGridData();
                });
            });
        },
        getFolders(folderPath){
            let _self = this;
            axios
            .post("/admin/getFoldersByPath", folderPath)
            .then(function (response) {
                _self.folderList = response.data.data;
            })
            .catch(function (error) {
                console.log(error);
            });
        },
        renderContent: function (h, { node, data, store }) {
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
    handleNodeClick(indata) {
      let _self = this;
      _self.currentFolder = indata;
      if (indata.extended == false) {
        _self.loading = true;
        axios
          .post("/admin/getFolder", indata.id)
          .then(function (response) {
            indata.children = response.data.data;
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
      _self.loadGridData(indata);
    },
    transferDataList(){
        let _self = this;
        this.$emit("selectchange", _self.finalDataList);
    },
    searchItem(){
      let _self = this;
      if(_self.currentFolder.length == 0){
          _self.$message({
                showClose: true,
                message: '请到文件夹下进行搜索',
                duration: 2000,
                type: "warning"
            });
      }else{
          _self.loadGridData(_self.currentFolder);
      }
     
    }
},

}
</script>
<style scoped>
    .el-footer{
        text-align: right;
    }
</style>