<template>
    <DataLayout>
        <template v-slot:header>
            <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false" :append-to-body="true">
                <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
                    <div style="height:150px;overflow-y:scroll; overflow-x:scroll;">
                    <el-upload
                        :limit="1"
                        :file-list="fileListA"
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
            <template>
            <ShowProperty
            v-if="allowEdit"
            ref="ShowProperty"
            @onSaved="onSaved"
            width="100%"
            folderPath
            :showUploadFile="showUploadFile"
            v-bind:typeName="typeName"
            :itemId="formId"
            ></ShowProperty>
            <ShowPropertyReadOnly
            v-else
            ref="ShowProperty"
            width="100%"
            :itemId="formId"
            :typeName="typeName"
            ></ShowPropertyReadOnly>
            </template>
        </template>
        <template v-slot:main="{layout}">
            <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
                    <template >
                        <el-form :inline="true" :model="filters" @submit.native.prevent>
                            <el-form-item>
                                <el-button type="primary" @click="beforeUploadFile4Mainfile('/dc/updatePrimaryContent')">替换电子文件</el-button> 
                            </el-form-item>
                        </el-form>
                        <DataGrid
                            ref="mainDataGrid"
                            key="main"
                            dataUrl="/dc/getDocuByRelationParentId"
                            :parentId="formId"
                            :tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                            :isshowOption="true" 
                            :isshowSelection ="true"
                            gridViewName="ModifyDocGrid"
                            condition=" and a.name = 'irel_children' "
                            :optionWidth = "2"
                            :isshowCustom="false"
                            :isEditProperty="isEdit"
                            showOptions="查看内容,查看属性"
                            :isShowPropertyButton="false"
                            :isShowChangeList="false"
                            @rowclick="rowClick"
                            @selectchange="selectChange"
                        ></DataGrid>
                    </template>
                    <template >
                        <el-tabs  v-model="selectedTabName">
                            <el-tab-pane  label="流程文件" name="t03" >
                                <el-row v-if="allowChangeDoc">
                                    <el-col :span="24">
                                        <el-form :inline="true" :model="filters" @submit.native.prevent>
                                            <!-- <el-form-item>
                                            <el-button type="primary" @click="beforeUploadFile('/dc/addAttachment')">{{$t('application.new')}}</el-button>
                                            </el-form-item>
                                            <el-form-item>
                                            <el-button type="warning" @click="onDeleleItem(selectedAttachment,[$refs.attachmentDoc])">{{$t('application.delete')}}</el-button>
                                            </el-form-item> -->
                                            <el-form-item>
                                            <el-button type="primary" @click="beforeUploadFile('/dc/updatePrimaryContent')">替换电子文件</el-button> 
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
                                    gridViewName="ResearchBorrowGrid"
                                    condition=" and a.NAME='irel_children'"
                                    :optionWidth = "2"
                                    :isshowCustom="false"
                                    :isEditProperty="true"
                                    :isShowMoreOption="true"
                                    showOptions="查看内容,查看属性"
                                    :isShowChangeList="false"
                                    :isShowPropertyButton="false"
                                    @rowclick="rwClick"
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
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly.vue";
import DataGrid from "@/components/DataGrid";
import BatchImport from '@/components/controls/ImportDocument';
import DataSelect from '@/components/ecm-data-select'
import MountFile from '@/components/MountFile.vue';
import AttachmentFile from "@/views/dc/AttachmentFile.vue"
import DataLayout from '@/components/ecm-data-layout'
export default {
    // CNPE 待提交文函
    data(){
        return{
            topStorageName: 'SubmissiondcHeight',
            startHeight: 135,
            topPercent: 50,
            topbarHeight: 35,
            bottomHeight: 155,
            formLabelWidth: "120px",
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
            childrenTypeSelectVisible:false,
            selectedChildrenType:'',
            selectedTransferDocItems:[],
            parentId:"",
            selectRow:[],
            relationName:"",
            importdialogVisible:false,
            fileListA: [],
            uploading:false,
            selectedAttachment:[],
            uploadUrl:'',
            gridObj:[],
            rightTableHeight: (window.innerHeight - 60)/2,
            relation:{},
            isShowRelevant:true,
            selectedTabName:'t03',
            docId:"",
            isOnly:false,
            changeDetail:"",
            parentId4Update:"",
            isEdit:true,
            parentId4UpdateMain:"",
            isUpdateMain:true
            //importdialogVisible4Update:false
        }
    },
    created(){
        
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
        rwClick(val){
            this.parentId4Update = val.ID
        },
        handleChange(file, fileList) {
            this.fileListA = fileList;
        },
        beforeUploadFile4Mainfile(uploadpath){
            let _self=this;
            if(_self.parentId4UpdateMain==undefined||_self.parentId4UpdateMain==''){
                _self.$message({
                        showClose: true,
                        message: "请单击流程文件主表里任意一条数据来进行替换电子文件!",
                        duration: 2000,
                        type: "warning"
                    });
                return;
            }
            _self.isUpdateMain=true
            _self.uploadUrl=uploadpath;
            _self.fileListA=[];
            _self.importdialogVisible=true;
        },
        beforeUploadFile(uploadpath){
            let _self=this;
            if(_self.parentId4Update==undefined||_self.parentId4Update==''){
                _self.$message({
                        showClose: true,
                        message: "请单击流程文件子表里任意一条数据来进行替换电子文件!",
                        duration: 2000,
                        type: "warning"
                    });
                return;
            }
            _self.uploadUrl=uploadpath;
            _self.fileListA=[];
            _self.importdialogVisible=true;
        },
          uploadData() {
            let _self = this;
            let formdata = _self.getFormData();
            _self.uploading=true;
            if(this.isUpdateMain==true){
            formdata.append("id",_self.parentId4UpdateMain)
            }
            else if(this.isUpdateMain==false){
            formdata.append("id",_self.parentId4UpdateMain)
            }
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
                _self.uploading=false;
                _self.$refs.attachmentDoc.loadGridData()
                _self.$refs.mainDataGrid.loadGridData()
                _self.parentId4Update=""
                _self.parentId4UpdateMain=""
                _self.isUpdateMain=false
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

        limitMessage(){
        this.$message({
                showClose: true,
                message:"仅可以上传一个电子文件",
                duration: 2000,
                type: 'warning'
                });
        },
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
        getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.parentId;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='irel_children';
            data["TYPE_NAME"]='附件';
            data["C_COMMENT1"]=_self.changeDetail
            _self.changeDetail=""
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
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
        // uploadData() {
        //     let _self = this;
        //     let formdata = _self.getFormData();
        //     _self.uploading=true;
        //     _self
        //         .axios({
        //         headers: {
        //             "Content-Type": "application/json;charset=UTF-8"
        //         },
        //         datatype: "json",
        //         method: "post",
        //         data: formdata,
        //         url: _self.uploadUrl
        //         })
        //         .then(function(response) {
        //         _self.importdialogVisible = false;
        //         _self.uploading=false;
        //         _self.$refs.attachmentDoc.loadGridData();
        //         _self.$message({
        //                 showClose: true,
        //                 message: _self.$t('application.Import')+_self.$t('message.success'),
        //                 duration: 2000,
        //                 type: 'success'
        //             });
        //         })
        //         .catch(function(error) {
        //         _self.uploading=false;
        //         console.log(error);
        //         });
        //     },
        rowClick(row){
            this.selectRow=row;
            this.parentId4UpdateMain = row.ID
             if(row.C_ITEM_TYPE=='案卷'){
                 this.isEdit=false
             }
             if(row.C_ITEM_TYPE!='案卷'){
                 this.isEdit=true
             }
            console.log(this.isEdit)
            this.parentId=row.ID;
            let _self=this;
            _self.$nextTick(()=>{
                    _self.$refs.attachmentDoc.parentId=row.ID;
                    _self.$refs.attachmentDoc.loadGridData();
               });
        },
        // 表格行选择
        selectChange(val) {
            this.selectedItems = val;
        }

    },
    props: {
        formId: {type: String,default: ""},
        allowEdit: { type: Boolean, default: false },
        showUploadFile: { type: Boolean, default: true },
        processDefinitionId: { type: String, default: "" },
        activityName: { type: String, default: "" },
        allowChangeDoc: { type: Boolean, default: true }
    },
    components: {
        ShowProperty:ShowProperty,
        ShowPropertyReadOnly: ShowPropertyReadOnly,
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