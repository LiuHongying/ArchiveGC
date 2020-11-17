<template>
    <DataLayout>
        <template v-slot:header>
            <!-- 创建附件 -->
            <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false">
                <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
                    <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
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
                    </div>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
                    <el-button type="primary" @click="uploadData()">{{$t('application.start')+$t('application.Import')}}</el-button>
                </div>
            </el-dialog>
            <el-form :inline="true" :model="filters" @submit.native.prevent>
            <el-form-item>
                <el-button type="primary" @click="beforImport($refs.mainDataGrid,false,'','/系统配置/导入模板/文函')">{{$t('application.Manual')+$t('application.Import')}}</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="updateDocContent()">{{$t('application.replace')}}</el-button>
                <!-- <MountFile :selectedItem="selectedItems" @refresh='searchItem' :title="$t('application.ReplaceDoc')">{{$t('application.replace')}}</MountFile> -->
            </el-form-item>
            </el-form>
        </template>
        <template v-slot:main="{layout}">
            <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
                <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
                    <template slot="paneL">
                        <DataGrid
                            ref="mainDataGrid"
                            key="main"
                            dataUrl="/dc/getDocuByRelationParentId"
                            parentId="bbf618d54f344957a0bec276da88a703"
                            v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                            v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                            gridViewName="ModifyDocGrid"
                            condition=" and a.name = 'irel_children' "
                            :optionWidth = "2"
                            :isshowCustom="false"
                            :isEditProperty="true"
                            showOptions="查看内容"
                            :isShowChangeList="false"
                            @rowclick="rowClick"
                            @selectchange="selectChange"
                        ></DataGrid>
                    </template>
                    <template slot="paneR">
                        <el-tabs  v-model="selectedTabName">
                            <el-tab-pane  :label="$t('application.Attachment')" name="t03" >
                                <el-row>
                                    <el-col :span="24">
                                        <el-form :inline="true" :model="filters" @submit.native.prevent>
                                            <el-form-item>
                                            <el-button type="primary" @click="beforeUploadFile('/dc/addAttachment')">{{$t('application.new')}}</el-button>
                                            </el-form-item>
                                            <el-form-item>
                                            <el-button type="warning" @click="onDeleleItem(selectedAttachment,[$refs.attachmentDoc])">{{$t('application.delete')}}</el-button>
                                            </el-form-item>
                                        </el-form>
                                    </el-col>
                                </el-row>
                                <!--列表-->
                                <DataGrid
                                    ref="attachmentDoc"
                                    key="attachmentDocKey"
                                    dataUrl="/dc/getDocuByRelationParentId"
                                    v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                                    v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                                    gridViewName="AttachmentGrid"
                                    condition=" and a.NAME='irel_children'"
                                    :optionWidth = "2"
                                    :isshowCustom="false"
                                    :isEditProperty="true"
                                    showOptions="查看内容"
                                    :isShowChangeList="false"
                                    @selectchange="attachmentDocSelect"
                                ></DataGrid>
                            </el-tab-pane>
                        </el-tabs>
                    </template>
                </split-pane>
            </div>
        </template>
    </DataLayout>
</template>

<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import BatchImport from '@/components/controls/ImportDocument';
import DataSelect from '@/components/ecm-data-select'
import MountFile from '@/components/MountFile.vue';
import AttachmentFile from "@/views/dc/AttachmentFile.vue"
import DataLayout from '@/components/ecm-data-layout'
export default {
    // CNPE 待提交文函
    name: "Submissiondc",
    data(){
        return{
            // 本地存储高度名称
            topStorageName: 'SubmissiondcHeight',
            // 非split pan 控制区域高度
            startHeight: 135,
            // 顶部百分比*100
            topPercent: 65,
            // 顶部除列表高度
            topbarHeight: 35,
            // 底部除列表高度
            bottomHeight: 120,
            buttLoading:false,
            filters: {
                projectCode: "",
                docType: "",
                coding: "",
                title: "",
                limit: 10,
                typeName:'',
                relationName:'',
            },
            dialog:{
                title:"",
                visible:false
            },
            typeName:"文件传递单",
            dialogName:"文件传递单",
            propertyVisible:false,
            selectedItems: [],
            childrenTypes:[],
            childrenTypeSelectVisible:false,
            selectedChildrenType:'',
            selectedTransferDocItems:[],
            parentId:"",
            selectRow:[],
            relationName:"",
            relevantDocSelected:[],
            importdialogVisible:false,
            fileList: [],
            uploading:false,
            selectedAttachment:[],
            uploadUrl:'',
            batchDialogVisible:false,
            gridObj:[],
            rightTableHeight: (window.innerHeight - 60)/2,
            relation:{},
            isShowDesgin:true,
            isShowRelevant:true,
            isShowAttachmentDoc:true,
            selectedTabName:'t03',
            importSubVisible:false,
            docId:"",
            isOnly:false,
            butt:false,
            propertyrela:false,
            inputValueNum:'',
            isShowMeet:true,
            MeetDocSelected:[],
            isShowMaterial:true,
            MaterialDocSelected:[],
            MeetMaterialDialogVisible:false,
        }
    },
    created(){
        this.getTypeNamesByMainList("DCTypeSubContractor");
        
    },
    mounted(){
        if(!this.validataPermission()){
            //跳转至权限提醒页
            let _self=this;
            _self.$nextTick(()=>{
                _self.$router.push({ path: '/NoPermission' })
            })
            
        }
        setTimeout(() => {
            this.topPercent = this.getStorageNumber(this.topStorageName,60)
        }, 300);
    },
    methods: {
        // 上下分屏事件
        onSplitResize(topPercent){
            // 顶部百分比*100
            this.topPercent = topPercent
            this.setStorageNumber(this.topStorageName, topPercent)
            //console.log(JSON.stringify(topPercent))
        },
        dbClick(row){
            this.docId=row.ID;
            this.dialog.visible=true;
            
            this.$nextTick(()=>{
                this.$refs.subAttachment.refresh();
                // this.$refs.subAttachment.docId=row.ID;
            });
        },
        refreshReleventDocData(){
            this.$refs.relevantDoc.loadGridData();
        },
        beforImport(obj,isSub,relationName,path){
            if(relationName=='设计文件'||relationName=='会议纪要内容项'||relationName=='材料变更清单'){
                if(this.parentId==''){
                    this.$message({
                    showClose: true,
                    message:this.$t('message.noMainFile'),
                    duration: 2000,
                    type: 'warning'
                    });
                    return;
                }
            }
            this.gridObj=obj;
            this.batchDialogVisible=true;
            this.$nextTick(()=>{
                if(isSub){
                    this.$refs.BatchImport.deliveryId=this.parentId;
                    this.$refs.BatchImport.relationName=relationName;
                    
                }else{
                    this.$refs.BatchImport.deliveryId='';
                    this.$refs.BatchImport.relationName='';
                }
                this.$refs.BatchImport.tmpPath=path;
                this.$refs.BatchImport.loadTemplate();
            })
        },
        attachmentDocSelect(val){
            this.selectedAttachment=val;
        },
        handleChange(file, fileList) {
            this.fileList = fileList;
        },
        beforeUploadFile(uploadpath){
            let _self=this;
            if(_self.parentId==undefined||_self.parentId==''){
                // _self.$message('请选择一条文件数据');
                _self.$message({
                        showClose: true,
                        message: _self.$t('message.PleaseSelectOneFile'),
                        duration: 2000,
                        type: "warning"
                    });
                return;
            }
            _self.uploadUrl=uploadpath;
            _self.fileList=[];
            _self.importdialogVisible=true;
        },

        getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.parentId;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='irel_children';
            data["TYPE_NAME"]='附件';
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
                //console.log(file.name);
                formdata.append("uploadFile", file.raw, file.name);
            });
            return formdata;
            },
        //通过传递父id和附件id更新文档的主文件
        updateDocContent(){
            let _self = this;
            if ( _self.selectedItems==undefined || _self.selectedItems.length<1) {
                _self.$message({
                    showClose: true,
                    message: _self.$t('message.PleaseSelectOneFile'),
                    duration: 2000,
                    type: "warning"
                    });
                    return;
            }
            if( _self.selectedAttachment==undefined || _self.selectedAttachment.length<1){
                _self.$message({
                    showClose: true,
                    message: _self.$t('message.PleaseSelectOneContent'),
                    duration: 2000,
                    type: "warning"
                    });
                    return;
            }
            var m = new Map();
            let formdata = new FormData();
            m.set("primaryId",_self.selectedItems[0].ID)
            m.set("contentId",_self.selectedAttachment[0].ID)
            formdata.append("metaData",JSON.stringify(m))
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url:'/dc/updateDcContentById'
                })
                .then(function(data){
                    console.log(data)
                })
                .catch(function(error) {
                _self.uploading=false;
                console.log(error);
                });
            
        },
        //上传文件
        uploadData() {
            let _self = this;
            let formdata = _self.getFormData();
            console.log("UploadData getData");
            console.log(formdata);
            _self.uploading=true;
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url: _self.uploadUrl
                })
                .then(function(response) {
                _self.importdialogVisible = false;
                // _self.refreshData();
                _self.uploading=false;
                _self.$refs.attachmentDoc.loadGridData();
                // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
                _self.$message({
                        showClose: true,
                        message: _self.$t('application.Import')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'success'
                    });
                })
                .catch(function(error) {
                _self.uploading=false;
                console.log(error);
                });
            },
            getSubFormData() {
                let _self = this;
                let formdata = new FormData();
                var data = {};
                data["parentDocId"] = _self.selectedTransferDocItems[0].ID;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
                data["relationName"]='附件';
                data["TYPE_NAME"]='附件';
                formdata.append("metaData", JSON.stringify(data));
                _self.fileList.forEach(function(file) {
                    //console.log(file.name);
                    formdata.append("uploadFile", file.raw, file.name);
                });
                return formdata;
            },
            //上传文件
        uploadDataSub() {
            let _self = this;
            let formdata = _self.getSubFormData();
            console.log("UploadData getData");
            console.log(formdata);
            _self.uploading=true;
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url: _self.uploadUrl
                })
                .then(function(response) {
                _self.importSubVisible = false;
                // _self.refreshData();
                _self.uploading=false;
                // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
                _self.$message({
                        showClose: true,
                        message: _self.$t('application.Import')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'success'
                    });
                })
                .catch(function(error) {
                _self.uploading=false;
                console.log(error);
                });
            },
            changePage(key){
                let _self=this;
                _self.getRelatinItemByTypeName(row.TYPE_NAME,_self.$refs.relevantDoc,function(val){
                    _self.relation=val;
                    // _self.$refs.relevantDoc.loadGridInfo();
                    // _self.$refs.relevantDoc.loadGridData();
                });
            },
        rowClick(row){
            this.selectRow=row;
            this.parentId=row.ID;
            let _self=this;
            _self.$nextTick(()=>{
                    _self.$refs.attachmentDoc.parentId=row.ID;
                    _self.$refs.attachmentDoc.loadGridData();
               });
        },
        MeetDocSelect(val){
            this.MeetDocSelected=val;
        },
        MaterialDocSelect(val){
            this.MaterialDocSelected=val;
        },
        relevantDocSelect(val){
            this.relevantDocSelected=val;
        },
        clickNewItem(){
            let _self=this;
            
            _self.childrenTypeSelectVisible=true;
        },
        //获取类型
        getTypeNamesByMainList(keyName) {
            let _self = this;
            axios
                .post("/dc/getOneParameterValue", keyName)
                .then(function(response) {
                _self.childrenTypes = response.data.data;
                })
                .catch(function(error) {
                console.log(error);
                });
            },
        searchItem(){
            let _self=this;
            let key=" (status='' or status is null or status='新建') and C_COMPANY='@company'";
            if(_self.filters.projectCode!=''){
                key+=" and C_PROJECT_NAME = "+_self.filters.projectCode;
            }else{
                key+=" and C_PROJECT_NAME = '@project'";
            }
            if(_self.filters.docType!=''){
                key+=" and TYPE_NAME = '"+_self.filters.docType+"'";
            }
            if(_self.filters.title!=''){
                key+=" and (C_CONTENT like '%"+_self.filters.title+"%' "
                +"or C_FROM like '%"+_self.filters.title+"%' "
                +"or C_TO like '%"+_self.filters.title+"%' "
                +"or CODING like '%"+_self.filters.title+"%' "
                +"or C_OTHER_CODING like '%"+_self.filters.title+"%' "
                +")";
            }
            if(key!=''){
                _self.$refs.mainDataGrid.condition=key;
            }
            _self.$refs.mainDataGrid.loadGridData();
            if(_self.$refs.attachmentDoc!=undefined){
                _self.$refs.attachmentDoc.itemDataList=[];
            }
    
            _self.parentId='';
        },
        // 表格行选择
        selectChange(val) {
            this.selectedItems = val;
        }

    },
    props: {
        
    },
    components: {
        ShowProperty:ShowProperty,
        DataGrid:DataGrid,
        DataSelect:DataSelect,
        BatchImport:BatchImport,
        MountFile:MountFile,
        AttachmentFile:AttachmentFile,
        DataLayout:DataLayout
    }
}
</script>
<style scoped>

</style>