<template>
    <DataLayout>
    <template v-slot:header>
        <el-dialog :title="'创建'+fileType" 
        :visible.sync="createFileVisible" 
        @close="createFileVisible = false"
        width="80%">
            <!-- <CreateDocFile ref="createDocFile" :fileType="fileType" :selectedItemId="docId" @saveOrstartSuccess="closeCreateDialog"></CreateDocFile> -->
            <FormWithFile ref="formWithFile" :templateDoc="templateDoc" :childType="childType" 
            :selectedItemId="docId" :fileType="fileType" :allowEdit="true" :isShowReject="true"></FormWithFile>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="saveOrstart(true)">发起流程</el-button>
                <el-button type="success" @click="saveOrstart(false)">暂 存</el-button>
                <el-button @click="closeCreateDialog" size="medium">{{$t('application.close')}}</el-button>
            </div>
        </el-dialog>
       
    </template>
    <template v-slot:main="{layout}">
        <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="vertical">
          <template slot="paneL">
              <el-tree
                style="width: 100%"
                :props="defaultProps"
                :data="dataList"
                node-key="id"
                :render-content="renderContent"
                default-expand-all
                highlight-current
                @node-click="handleNodeClick"
              ></el-tree>
          <!-- </el-container> -->
          </template>
        <template slot="paneR">
          <el-row>
          <el-form :inline="true">
            <el-form-item>
                <el-button type="primary" plain size="medium" icon="el-icon-download" @click="downloadTemplate">模板下载</el-button>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" plain size="medium" icon="el-icon-plus" @click="newObject">新建文件</el-button>
            </el-form-item>
            
          </el-form>
          </el-row>
          <el-row :style="'height:'+layout.height-startHeight+80">
            <PdfViewer v-if="templateDoc.format=='pdf'" v-bind:id="templateDoc.id" v-bind:format="templateDoc.format"></PdfViewer>
             <OfficeDocViewer v-else-if="templateDoc.format=='docx'" v-bind:id="templateDoc.id" v-bind:format="templateDoc.format"></OfficeDocViewer>
             
          </el-row>
        </template>
        </split-pane>
      </div>
    </template>
    </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import PdfViewer from '@/views/dc/PdfViewer2.6es5.vue';
import OfficeDocViewer from '@/views/dc/OfficeDocViewer.vue';
import CreateDocFile from '@/views/npc/CreateDocFile.vue'
import FormWithFile from '@/views/archivecd/FormWithFile.vue'
export default {
    components: {
        DataLayout: DataLayout,
        PdfViewer:PdfViewer,
        OfficeDocViewer:OfficeDocViewer,
        CreateDocFile:CreateDocFile,
        FormWithFile:FormWithFile
    },
    data(){
        return{
            // 非split pan 控制区域高度
            startHeight: 130,
            // 顶部百分比*100
            topPercent: 20,
            // 顶部除列表高度
            topbarHeight: 35,
            // 底部除列表高度
            bottomHeight: 120,
            buttLoading:false,
            defaultProps: {
                children: "children",
                label: "name"
            },
            dataList:[],
            loading:false,
            currentFolder: [],
            templateDoc:{
                id:"",
                format:""
            },
            fileType:"",
            childType:"",
            createFileVisible:false,
            docId:""
        }
    },
    props:{

    },
    created(){

    },
    mounted(){
        let _self = this;
        _self.loading = true;
        axios
            .post("/dc/getOneParameterValue", "npicTemplatePath")
            .then(function(response) {
            let docPath = response.data.data;
            axios
                .post("/admin/getFoldersByPath", docPath[0])
                .then(function (response) {
                _self.dataList = response.data.data;
                console.log(JSON.stringify(_self.dataList));
                _self.loading = false;
                })
                .catch(function (error) {
                console.log(error);
                _self.loading = false;
                });
            })
            .catch(function(error) {
                console.log(error);
            });
    },
    
    methods:{
        closeCreateDialog(){
            this.createFileVisible=false;
        },
        saveOrstart(isStart){
            this.$refs.formWithFile.saveOrStartup(isStart);
        },
        newObject(){
            
            let _self=this;
            let m=new Map();
            m.set("typeName",_self.fileType);
            
            axios
            .post("/cd/dc/newDocumentWithTemplate", JSON.stringify(m))
            .then(function (response) {
                if(response.data.code=='1'){
                    _self.docId=response.data.id;
                    _self.createFileVisible=true;
                }else{
                    _self.$message({
                        showClose: true,
                        message: response.data.message,
                        duration: 2000,
                        type: 'error'
                    });
                }
                
            })
            .catch(function (error) {
                console.log(error);
            });
        },
        downloadTemplate(){
            let _self = this;
            // if(_self.deliveryId==null || _self.deliveryId.length==0){
            //   _self.$message("请选择移交单导入!");
            //   return;
            // }
            if(_self.templateDoc.id==null || _self.templateDoc.id==""){
                 _self.$message({
                    showClose: true,
                    message:_self.$t('application.PleaseSelectTemplate'),
                    duration: 2000,
                    type: 'warning'
                });
                return;
            }
            // 拦截器会自动替换成目标url
            let url = this.axios.defaults.baseURL+"/dc/getContent?id="+_self.templateDoc.id+"&token="+sessionStorage.getItem('access-token');
            window.open(url);
        },
        onSplitResize(topPercent){
            // 顶部百分比*100
            this.topPercent = topPercent
            this.setStorageNumber(this.topStorageName, topPercent)
            //console.log(JSON.stringify(topPercent))
        },
        // 文件夹节点点击事件
        handleNodeClick(indata) {
            let _self = this;
            _self.currentFolder = indata;
            // _self.typeName=indata.typeName;
            if (indata.extended == false) {
                _self.loading = true;
                axios
                .post("/cd/dc/getTemplateTreeData", indata.id)
                .then(function (response) {
                    indata.children = response.data.data;
                    //console.log(JSON.stringify(indata));
                    
                    indata.extended = true;
                    _self.loading = false;
                })
                .catch(function (error) {
                    console.log(error);
                    _self.loading = false;
                });
            }else{
                _self.fileType=indata.attributes['C_TYPE1'];
                _self.childType=indata.attributes['C_TYPE2'];
                _self.templateDoc.id=indata.id;
                _self.templateDoc.format=indata.formatName;
            }
           
        },
        renderContent: function(h, { node, data, store }) {
            //console.log(data);
            if (data.extended) {
                return (
                <span>
                    <i class="el-icon-folder-opened"></i>
                    <span style="padding-left: 4px;">{node.label}</span>
                </span>
                );
            } else {
                if(data.formatName){
                     return (
                        <span>
                            <i class="el-icon-document"></i>
                            <span style="padding-left: 4px;">{node.label}</span>
                        </span>
                    );
                }else{
                    return (
                        <span>
                            <i class="el-icon-folder"></i>
                            <span style="padding-left: 4px;">{node.label}</span>
                        </span>
                    );
                }
                
            }
        },
    }
    
}
</script>
<style scoped>

</style>