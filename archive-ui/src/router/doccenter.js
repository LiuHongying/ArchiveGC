export const dcRouter = [
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'companyDoc'
			},
			path: '/dc/companydoc',
			component: () => import('@/views/dc/CompanyDoc.vue'),
			name: '库存档案',
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'docStorage'
			},
			path: '/dc/docStorage',
			component: () => import('@/views/dc/DocStorage.vue'),
			name: '文件库',
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'preArchive'
			},
			path: '/dc/preArchive',
			component: () => import('@/views/dc/PreArchiveStorage.vue'),
			name: '预归档库',
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'recordReport'
			},
			path: '/record/recordReport',
			component: () => import('@/views/record/RecordReport.vue'),
			name: '报表',
		},
		{
			meta: {
				requireAuth: true,
				permit: 1
			},
			path: '/borrow',
			name: '借阅单',
			component: () => import('@/components/form/Borrow.vue')
			
		}, 
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'folderViewer'
			},
			path: '/dc/folderviewer',
			component: () => import('@/views/dc/FolderViewer.vue'),
			name: '查看文件夹'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'folderViewer'
			},
			path: '/dc/folder',
			component: () => import('@/views/dc/FolderClassification.vue'),
			name: '文件夹浏览'
		},
		{
			meta: {
				requireAuth: true,
				permit:1
			},
			path: '/dc/officedocviewer',
			component: () => import('@/views/dc/OfficeDocViewer.vue'),
			name: '文档浏览'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1
			},
			path: '/dc/imageviewer',
			component: () => import('@/views/dc/ImageViewer.vue'),
			name: '图像浏览'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1
			},
			path: '/dc/videoplayer',
			component: () => import('@/views/dc/VideoPlayer.vue'),
			name: '视频播放'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1
			},
			path: '/dc/audioplayer',
			component: () => import('@/views/dc/AudioPlayer.vue'),
			name: '音频播放'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'recycleBin'
			},
			path: '/dc/recyclebin',
			component: () => import('@/views/dc/RecycleBin.vue'),
			name: '回收站'
		},
		{
			meta: {
				requireAuth: true,
				permit: 1,
				title:'folderViewer'
			},
			path: '/dc/JTViewer',
			component: () => import('@/views/dc/JTViewer.vue'),
			name: 'JT查看'
		}
	]
