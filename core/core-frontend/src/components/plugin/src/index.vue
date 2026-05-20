<script lang="ts" setup>
import { onMounted } from 'vue'

const emit = defineEmits([
  'loaded',
  'load-fail',
  'load-ds-plugin',
  'load-plugin-category'
])

defineExpose({
  invokeMethod: () => undefined
})

onMounted(() => {
  emit('loaded')
  emit('load-fail')
  emit('load-ds-plugin', [])
  emit('load-plugin-category', [])
})
</script>

<template>
  <span class="de-lite-plugin-placeholder" />
</template>
