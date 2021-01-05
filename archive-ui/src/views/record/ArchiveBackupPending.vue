<template>
    <div>
      <el-dialog width="80%" title="列表" :visible.sync="archiveBackupVisible" @close="archiveBackupVisible = false">
        <el-row>
            <el-col>
                <DataGrid ref="orderGrid" key="main" v-bind:itemDataList="childDataList"
                      v-bind:columnList="childColumnList" @pagesizechange="childPageSizeChange"
                      @pagechange="childPageChange" v-bind:itemCount="childItemCount"
                      v-bind:tableHeight="rightTableHeight" :isshowOption="false"
                       ></DataGrid>
            </el-col>

        </el-row>
        <div slot="footer" class="dialog-footer">
          <!-- <el-button @click="saveItem">{{$t('application.save')}}</el-button>  -->
          <el-button @click="archiveBackupVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
        
        <el-row>
            <el-col>
                <DataGrid ref="orderGrid" key="main" 
                      v-bind:tableHeight="rightTableHeight" :isshowOption="true"
                      :loading="orderLoading"
                      :isshowicon="false"
                      :optionWidth = "1"
                      :isShowChangeList="false"
                      :isShowMoreOption="false"
                      condition="STATUS='制作中'"
                      gridViewName="ArchiveBackup"
                      dataUrl="/dc/getDocuments"
                     ></DataGrid>
            </el-col>

        </el-row>
        
    </div>
</template>
<script type="text/javascript">
import DataGrid from'@/components/DataGrid';
export default {
    name:'archiveBackupNew',
    permit:1,
    data(){
        return{
          archiveBackupVisible:false,
             gridList:[],
             itemDataList:[],
             itemDataListFull:[],
             pageSize: 20,
             currentPage: 1,
             itemCount: 0,
             childCurrentPage:1,
             childPageSize: 20,
             childItemCount:0,
             childDataList:[],
             childColumnList:[],
             orderLoading:false,
             selectedRow:[],
            form: {
              coding:"",
              title:"",
              createDate:"",
              endDate:"",
              size:0,
              director:"",
              condition:""
            },
             rightTableHeight: window.innerHeight - 130
        }
       
    },
    mounted(){
        //this.loadGridInfo();
        this.loadGridData();
    },
    components:{
        DataGrid:DataGrid
    },
    methods:{
        loadGridData() {
          let _self = this;
          _self.orderLoading=true;
          var key0 = "";
          if (key0 != "") {
            key0 = " (coding like '%" + key0 + "%' or C_DRAFTER like '%" + key0 + "%') and STATUS='制作中' ";
          }else{
              key0=" STATUS='制作中' "
          }
        
          var m = new Map();
          m.set("gridName", "ArchiveBackup");
          // m.set('folderId',indata.id);
          m.set("condition", key0);
          
          m.set("pageSize", _self.pageSize);
          m.set("pageIndex", (_self.currentPage - 1) * _self.pageSize);
          m.set("orderBy", "");
          // console.log('pagesize:', _self.pageSize);
          axios.post("/dc/getBorrowOrder",JSON.stringify(m))
            .then(function(response) {
              _self.itemDataList = response.data.data;
              _self.itemDataListFull = response.data.data;
              _self.itemCount = response.data.pager.total;
              //console.log(JSON.stringify(response.data.datalist));
              _self.orderLoading = false;
            })
            .catch(function(error) {
              console.log(error);
              _self.orderLoading = false;
            });
        },
        }

};
</script>
<style scoped>

</style>